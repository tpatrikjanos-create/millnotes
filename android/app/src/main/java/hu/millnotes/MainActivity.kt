package hu.millnotes

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.webkit.WebViewAssetLoader
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private lateinit var web: WebView
    private var pendingFileChooser: ValueCallback<Array<Uri>>? = null
    private var pendingSaveText: String? = null
    private var pendingShared: String? = null   // más appból megosztott szöveg, amíg az oldal betölt
    private var pageReady = false

    // Import: fájl kiválasztása
    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        pendingFileChooser?.onReceiveValue(uri?.let { arrayOf(it) })
        pendingFileChooser = null
    }

    // Export: a mentés helyének kiválasztása, majd a szöveg kiírása
    private fun writeTo(uri: Uri?, okMsg: String) {
        val text = pendingSaveText
        pendingSaveText = null
        if (uri != null && text != null) {
            contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
            Toast.makeText(this, okMsg, Toast.LENGTH_SHORT).show()
        }
    }
    private val createJson = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { writeTo(it, "Export elkészült") }
    private val createText = registerForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { writeTo(it, "Markdown-fájl elkészült") }

    // A JavaScriptből hívható híd: window.AndroidBridge
    inner class Bridge {
        @JavascriptInterface
        fun saveFile(name: String, data: String) = runOnUiThread { pendingSaveText = data; createJson.launch(name) }

        @JavascriptInterface
        fun saveText(name: String, data: String) = runOnUiThread { pendingSaveText = data; createText.launch(name) }

        @JavascriptInterface
        fun shareText(title: String, text: String) = runOnUiThread {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(send, "Jegyzet megosztása"))
        }

        @JavascriptInterface
        fun openUrl(url: String) = runOnUiThread { openExternal(url) }

        // Az oldal induláskor elkéri a közben érkezett megosztott szöveget
        @JavascriptInterface
        fun takeShared(): String {
            val s = pendingShared ?: ""
            pendingShared = null
            pageReady = true
            return s
        }
    }

    private fun openExternal(url: String) {
        val uri = Uri.parse(url)
        if (uri.scheme != "http" && uri.scheme != "https") return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Nincs böngésző a link megnyitásához", Toast.LENGTH_SHORT).show()
        }
    }

    // Más appból (Megosztás → MillNotes) érkező szöveg
    private fun handleShare(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type?.startsWith("text/") != true) return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
        val json = JSONObject().put("subject", subject).put("text", text).toString()
        if (pageReady) {
            web.evaluateJavascript("window.appShareIn && window.appShareIn(${JSONObject.quote(subject)}, ${JSONObject.quote(text)})", null)
        } else {
            pendingShared = json
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // A HTML a helyi assets mappából töltődik, https-en, így a crypto.subtle és az EventSource működik
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web = WebView(this)
        web.setBackgroundColor(0xFF0D0A12.toInt())
        setContentView(web)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.addJavascriptInterface(Bridge(), "AndroidBridge")

        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                loader.shouldInterceptRequest(request.url)

            // Külső link ne az appban nyíljon meg, hanem a böngészőben
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.url.host == "appassets.androidplatform.net") return false
                openExternal(request.url.toString())
                return true
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                pendingFileChooser?.onReceiveValue(null)
                pendingFileChooser = callback
                pickFile.launch("*/*")
                return true
            }
        }

        // A vissza gomb először a JS-ben zárja be a nyitott részt (menü, jegyzet, mappa), csak utána lép ki
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                web.evaluateJavascript("(window.appBack && window.appBack()) ? 'y' : 'n'") { result ->
                    if (result != "\"y\"") finish()
                }
            }
        })

        handleShare(intent)
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShare(intent)
    }

    // Háttérbe kerülés előtt minden helyben mentődik, és elindul a feltöltés
    override fun onPause() {
        super.onPause()
        if (::web.isInitialized) web.evaluateJavascript("window.appPause && window.appPause()", null)
    }
}
