[CmdletBinding()]
param(
    [string]$PzDir = "${env:ProgramFiles(x86)}\Steam\steamapps\common\ProjectZomboid"
)

$ErrorActionPreference = 'Stop'
$InstallDir = Join-Path $PzDir 'PlayerBuiltRoomFix'
$ConfigPath = Join-Path $PzDir 'ProjectZomboid64.json'
$PatchArgument = '-agentlib:zbNative=patches_jar=PlayerBuiltRoomFix/PlayerBuiltRoomFix.jar:io.shaun.playerbuiltroomfix'
$PlainArgument = '-agentlib:zbNative'

if (@(Get-Process -Name 'ProjectZomboid64' -ErrorAction SilentlyContinue).Count -ne 0) {
    throw 'Project Zomboid is running. Close it before uninstalling.'
}

if (-not (Test-Path -LiteralPath $ConfigPath -PathType Leaf)) {
    throw "Launcher configuration not found: $ConfigPath"
}

$Config = Get-Content -LiteralPath $ConfigPath -Raw | ConvertFrom-Json
$VmArgs = @($Config.vmArgs)
$Matches = @()
for ($index = 0; $index -lt $VmArgs.Count; $index++) {
    if ($VmArgs[$index] -eq $PatchArgument) { $Matches += $index }
}
if ($Matches.Count -gt 1) {
    throw 'Multiple PlayerBuiltRoomFix agent arguments found; refusing to modify the launcher configuration.'
}
if ($Matches.Count -eq 1) {
    $VmArgs[$Matches[0]] = $PlainArgument
    $Config.vmArgs = $VmArgs
    $Config | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $ConfigPath -Encoding UTF8
}

if (Test-Path -LiteralPath $InstallDir -PathType Container) {
    $ResolvedInstall = [System.IO.Path]::GetFullPath($InstallDir)
    $ExpectedInstall = [System.IO.Path]::GetFullPath((Join-Path $PzDir 'PlayerBuiltRoomFix'))
    if (-not $ResolvedInstall.Equals($ExpectedInstall, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to remove unexpected path: $ResolvedInstall"
    }
    [System.IO.Directory]::Delete($ResolvedInstall, $true)
}

'PlayerBuiltRoomFix was removed. The pre-existing ZombieBuddy installation was left unchanged.'
