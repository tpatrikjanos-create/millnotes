using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Threading.Tasks;
using System.Windows.Forms;
using Microsoft.Web.WebView2.Core;
using Microsoft.Web.WebView2.WinForms;

static class Program
{
    [STAThread]
    static void Main()
    {
        ApplicationConfiguration.Initialize();
        Application.Run(new MainForm());
    }
}

sealed class MainForm : Form
{
    private readonly WebView2 web = new WebView2 { Dock = DockStyle.Fill };

    public MainForm()
    {
        Text = "MillNotes";
        Width = 1200;
        Height = 800;
        MinimumSize = new Size(640, 480);
        BackColor = Color.FromArgb(13, 10, 18);
        Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath);
        Controls.Add(web);
        Load += async (s, e) => await InitAsync();
    }

    private async Task InitAsync()
    {
        await web.EnsureCoreWebView2Async();

        // A web mappa az exe mellett van; https-en töltjük be, hogy a crypto.subtle és az EventSource működjön
        string webDir = Path.Combine(AppContext.BaseDirectory, "web");
        web.CoreWebView2.SetVirtualHostNameToFolderMapping(
            "jegyzet.local", webDir, CoreWebView2HostResourceAccessKind.Allow);

        web.CoreWebView2.Settings.AreDevToolsEnabled = false;
        // A jobb klikkes menü kell a szerkesztőben (kivágás, másolás, beillesztés, helyesírás)
        web.CoreWebView2.Settings.AreDefaultContextMenusEnabled = true;

        // Külső linkek a rendszer böngészőjében nyílnak meg, nem egy új MillNotes-ablakban
        web.CoreWebView2.NewWindowRequested += (s, e) =>
        {
            e.Handled = true;
            if (Uri.TryCreate(e.Uri, UriKind.Absolute, out var u) && (u.Scheme == "http" || u.Scheme == "https"))
                Process.Start(new ProcessStartInfo(e.Uri) { UseShellExecute = true });
        };
        web.CoreWebView2.NavigationStarting += (s, e) =>
        {
            if (e.Uri.StartsWith("http") && !e.Uri.StartsWith("https://jegyzet.local/"))
            {
                e.Cancel = true;
                Process.Start(new ProcessStartInfo(e.Uri) { UseShellExecute = true });
            }
        };

        web.Source = new Uri("https://jegyzet.local/index.html");
    }
}
