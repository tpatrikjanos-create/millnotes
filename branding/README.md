# MillNotes logó – átadás

Döntés (2026-11-22): a MillNotes logója a túristvándi vízimalom vonalas rajza, alatta egy jegyzetlap-köteggel. A ceruza épp írja a legalsó sort. Ugyanaz a malom, mint a MemoMill logóján, de ott könyv van, itt lapköteg.

## Fájlok
- `millnotes-logo-animated.svg` – fejlécbe és betöltéshez. A kerekek forognak, a ceruza újra és újra kiírja a sort. Átlátszó háttér, fehér vonalak, ezért sötét felületre való.
- `millnotes-logo.svg` – ugyanez állóképként.
- `millnotes-icon.svg` és `millnotes-icon-512.png` – app-ikon: sötét, lekerekített négyzet, egyszerűsített rajz (a lapon csak az írott sor látszik). Ebből készül az Android launcher-ikon és a Windows .ico, valamint a böngészőfül ikonja.

## Színek
- Vonalak: #eef0fa
- Kiemelés (lapköteg, ceruza, írott sor): színátmenet #fbbf24 → #fb7185 (borostyán → korall)
- Ikon háttere: #0d0a12, a bal felső sarokban #4a1d2a derengéssel

A MemoMill a hideg Aurora színeket (#22d3ee → #8b5cf6) használja, a MillNotes ezzel a meleg párral különül el tőle.

## Beépítés (a MillNotes repóban, „mehet” után)
1. A fájlok a `branding/` mappában vannak. Beépítéskor az SVG-ket az `index.html` mellé kell tenni (vagy inline beilleszteni). A fejlécben a „MillNotes” felirat elé kerüljön az animált logó, kb. 28–32 px magasan.
2. Favicon: a `millnotes-icon.svg` legyen `<link rel="icon" type="image/svg+xml">`.
3. Android: a 512 px-es PNG-ből készüljön az mipmap ikonkészlet (mdpi 48, hdpi 72, xhdpi 96, xxhdpi 144, xxxhdpi 192 px).
4. Windows: a PNG-ből készüljön .ico (16, 32, 48, 256 px), ez kerül a WebView2-projektbe.
5. Az előző, sötétkék malomkerekes logó (#1E3A8A) ezzel cserélődik.
