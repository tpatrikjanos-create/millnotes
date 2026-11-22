# Start menü-bejegyzés létrehozása a MillNotes.exe-hez.
# Használat: a publish mappából futtasd (ugyanott legyen a MillNotes.exe és a web mappa):
#   powershell -ExecutionPolicy Bypass -File .\install-startmenu.ps1

$exe = Join-Path $PSScriptRoot "MillNotes.exe"
if (-not (Test-Path $exe)) {
    Write-Error "Nem találom a MillNotes.exe-t ebben a mappában: $PSScriptRoot"
    exit 1
}

$menu = [Environment]::GetFolderPath("Programs")
$lnk  = Join-Path $menu "MillNotes.lnk"

$shell = New-Object -ComObject WScript.Shell
$short = $shell.CreateShortcut($lnk)
$short.TargetPath       = $exe
$short.WorkingDirectory = $PSScriptRoot
$short.Description      = "MillNotes"
$short.Save()

Write-Host "Kész. A Start menüben most ott van: MillNotes"
Write-Host "Parancsikon: $lnk"
