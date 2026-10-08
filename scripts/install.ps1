[CmdletBinding()]
param(
    [string]$PzDir = "${env:ProgramFiles(x86)}\Steam\steamapps\common\ProjectZomboid"
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$InstallDir = Join-Path $PzDir 'PlayerBuiltRoomFix'
$ConfigPath = Join-Path $PzDir 'ProjectZomboid64.json'
$BuiltJar = Join-Path $ProjectRoot 'dist\PlayerBuiltRoomFix.jar'
$PatchArgument = '-agentlib:zbNative=patches_jar=PlayerBuiltRoomFix/PlayerBuiltRoomFix.jar:io.shaun.playerbuiltroomfix'
$PlainArgument = '-agentlib:zbNative'
$BackupRoot = Join-Path $env:USERPROFILE 'Zomboid\backups\PlayerBuiltRoomFix'
$Timestamp = Get-Date -Format 'yyyy-MM-dd-HHmmss'
$BackupDir = Join-Path $BackupRoot $Timestamp

$RunningGame = @(Get-Process -Name 'ProjectZomboid64' -ErrorAction SilentlyContinue)
if ($RunningGame.Count -ne 0) {
    throw 'Project Zomboid is running. Close it before installing this render patch.'
}

foreach ($required in @($ConfigPath, (Join-Path $PzDir 'projectzomboid.jar'), (Join-Path $PzDir 'ZombieBuddy.jar'))) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) {
        throw "Required file not found: $required"
    }
}

& (Join-Path $ProjectRoot 'build.ps1') -PzDir $PzDir
if ($LASTEXITCODE -ne 0) { throw 'Build failed.' }
& (Join-Path $ProjectRoot 'scripts\integration-test.ps1') -PzDir $PzDir
if ($LASTEXITCODE -ne 0) { throw 'ZombieBuddy integration test failed.' }

New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
Copy-Item -LiteralPath $ConfigPath -Destination (Join-Path $BackupDir 'ProjectZomboid64.json')
if (Test-Path -LiteralPath $InstallDir -PathType Container) {
    Copy-Item -LiteralPath $InstallDir -Destination (Join-Path $BackupDir 'previous-install') -Recurse
}

$Config = Get-Content -LiteralPath $ConfigPath -Raw | ConvertFrom-Json
$VmArgs = @($Config.vmArgs)
$ExistingPatchIndexes = @()
$PlainIndexes = @()
for ($index = 0; $index -lt $VmArgs.Count; $index++) {
    if ($VmArgs[$index] -eq $PatchArgument) { $ExistingPatchIndexes += $index }
    if ($VmArgs[$index] -eq $PlainArgument) { $PlainIndexes += $index }
}

if ($ExistingPatchIndexes.Count -gt 1) {
    throw 'Multiple PlayerBuiltRoomFix agent arguments found; refusing to guess which one to replace.'
}
if ($ExistingPatchIndexes.Count -eq 0) {
    if ($PlainIndexes.Count -ne 1) {
        throw 'Expected exactly one plain ZombieBuddy agent argument in ProjectZomboid64.json.'
    }
    $VmArgs[$PlainIndexes[0]] = $PatchArgument
    $Config.vmArgs = $VmArgs
    $Config | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $ConfigPath -Encoding UTF8
}

if (Test-Path -LiteralPath $InstallDir -PathType Container) {
    $ResolvedInstall = [System.IO.Path]::GetFullPath($InstallDir)
    $ExpectedInstall = [System.IO.Path]::GetFullPath((Join-Path $PzDir 'PlayerBuiltRoomFix'))
    if (-not $ResolvedInstall.Equals($ExpectedInstall, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to replace unexpected path: $ResolvedInstall"
    }
    [System.IO.Directory]::Delete($ResolvedInstall, $true)
}

New-Item -ItemType Directory -Path $InstallDir -Force | Out-Null
Copy-Item -LiteralPath $BuiltJar -Destination (Join-Path $InstallDir 'PlayerBuiltRoomFix.jar') -Force
Copy-Item -LiteralPath (Join-Path $ProjectRoot 'README.md') -Destination (Join-Path $InstallDir 'README.md') -Force
Copy-Item -LiteralPath (Join-Path $ProjectRoot 'docs\TECHNICAL.md') -Destination (Join-Path $InstallDir 'TECHNICAL.md') -Force
Copy-Item -LiteralPath (Join-Path $ProjectRoot 'scripts\uninstall.ps1') -Destination (Join-Path $InstallDir 'uninstall.ps1') -Force

$Record = @(
    'PlayerBuiltRoomFix installation record',
    "Installed: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss K')",
    'Project Zomboid: 42.21.0 revision 4a0e9546ec',
    'ZombieBuddy: 2.3.2',
    "Patch JAR SHA-256: $((Get-FileHash -LiteralPath $BuiltJar -Algorithm SHA256).Hash)",
    "Backup: $BackupDir",
    'Patches:',
    '  zombie.iso.objects.IsoTree.isPlayerInsideARoom(IsoPlayer):boolean',
    '  zombie.iso.fboRenderChunk.FBORenderCutaways.IsCutawaySquare(CutawayWall,IsoGridSquare,IsoGridSquare,long):boolean'
)
$Record | Set-Content -LiteralPath (Join-Path $InstallDir 'INSTALL_RECORD.txt') -Encoding UTF8

[pscustomobject]@{
    InstalledJar = Join-Path $InstallDir 'PlayerBuiltRoomFix.jar'
    SHA256 = (Get-FileHash -LiteralPath (Join-Path $InstallDir 'PlayerBuiltRoomFix.jar') -Algorithm SHA256).Hash
    Backup = $BackupDir
    AgentArgument = $PatchArgument
} | Format-List
