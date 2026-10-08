[CmdletBinding()]
param(
    [string]$PzDir = "${env:ProgramFiles(x86)}\Steam\steamapps\common\ProjectZomboid"
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = $PSScriptRoot
$BuildDir = Join-Path $ProjectRoot 'build'
$StubClasses = Join-Path $BuildDir 'stub-classes'
$Classes = Join-Path $BuildDir 'classes'
$TestClasses = Join-Path $BuildDir 'test-classes'
$IntegrationClasses = Join-Path $BuildDir 'integration-classes'
$DistDir = Join-Path $ProjectRoot 'dist'
$JarOut = Join-Path $DistDir 'PlayerBuiltRoomFix.jar'
$PzJar = Join-Path $PzDir 'projectzomboid.jar'
$ZbJar = Join-Path $PzDir 'ZombieBuddy.jar'

$ExpectedPzHash = 'E1A69EB743EDE60B213A0FE7F8B83D4FCAB773036D256CC4543A336F3B058A33'
$ExpectedZbHash = '6DD95CEDCE60F03BF8B8CEFD0D19EB156230E0D54BFFA07DE9DA5212A06C7BE6'

foreach ($required in @($PzJar, $ZbJar)) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) {
        throw "Required file not found: $required"
    }
}

if ((Get-FileHash -LiteralPath $PzJar -Algorithm SHA256).Hash -ne $ExpectedPzHash) {
    throw 'Unsupported projectzomboid.jar. This patch is intentionally restricted to PZ 42.21.0 revision 4a0e9546ec.'
}

if ((Get-FileHash -LiteralPath $ZbJar -Algorithm SHA256).Hash -ne $ExpectedZbHash) {
    throw 'Unsupported ZombieBuddy.jar. Expected ZombieBuddy 2.3.2.'
}

$Javac = (Get-Command javac.exe -ErrorAction Stop).Source
$Java = (Get-Command java.exe -ErrorAction Stop).Source
$JarTool = (Get-Command jar.exe -ErrorAction Stop).Source
$Javap = (Get-Command javap.exe -ErrorAction Stop).Source

if (Test-Path -LiteralPath $BuildDir) {
    $resolvedBuild = [System.IO.Path]::GetFullPath($BuildDir)
    $resolvedRoot = [System.IO.Path]::GetFullPath($ProjectRoot)
    if (-not $resolvedBuild.StartsWith($resolvedRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to clean build directory outside project root: $resolvedBuild"
    }
    [System.IO.Directory]::Delete($resolvedBuild, $true)
}

New-Item -ItemType Directory -Path $StubClasses, $Classes, $TestClasses, $IntegrationClasses, $DistDir -Force | Out-Null

$StubSources = @(Get-ChildItem -LiteralPath (Join-Path $ProjectRoot 'build-support\stubs') -Recurse -File -Filter '*.java' | Select-Object -ExpandProperty FullName)
$MainSources = @(Get-ChildItem -LiteralPath (Join-Path $ProjectRoot 'src\main\java') -Recurse -File -Filter '*.java' | Select-Object -ExpandProperty FullName)
$TestSources = @(Get-ChildItem -LiteralPath (Join-Path $ProjectRoot 'src\test\java') -Recurse -File -Filter '*.java' | Select-Object -ExpandProperty FullName)
$IntegrationSources = @(Get-ChildItem -LiteralPath (Join-Path $ProjectRoot 'src\integrationTest\java') -Recurse -File -Filter '*.java' | Select-Object -ExpandProperty FullName)

& $Javac '--release' '17' '-d' $StubClasses @StubSources
if ($LASTEXITCODE -ne 0) { throw 'Stub compilation failed.' }

& $Javac '--release' '17' '-parameters' '-classpath' "$ZbJar;$StubClasses" '-d' $Classes @MainSources
if ($LASTEXITCODE -ne 0) { throw 'Patch compilation failed.' }

& $Javac '--release' '17' '-classpath' "$ZbJar;$StubClasses;$Classes" '-d' $TestClasses @TestSources
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }

& $Java '-classpath' "$ZbJar;$StubClasses;$Classes;$TestClasses" 'io.shaun.playerbuiltroomfix.PlayerBuiltExtensionCutawayPatchTest'
if ($LASTEXITCODE -ne 0) { throw 'Behavior tests failed.' }

& $Javac '--release' '17' '-classpath' "$ZbJar;$StubClasses;$Classes" '-d' $IntegrationClasses @IntegrationSources
if ($LASTEXITCODE -ne 0) { throw 'Integration-test compilation failed.' }

$IsoTreeSignature = (& $Javap '-classpath' $PzJar '-p' '-s' 'zombie.iso.objects.IsoTree') -join "`n"
if ($IsoTreeSignature -notmatch 'private boolean isPlayerInsideARoom\(zombie\.characters\.IsoPlayer\);\s+descriptor: \(Lzombie/characters/IsoPlayer;\)Z') {
    throw 'Exact IsoTree.isPlayerInsideARoom(IsoPlayer):boolean signature was not found.'
}

$CutawaySignature = (& $Javap '-classpath' $PzJar '-p' '-s' 'zombie.iso.fboRenderChunk.FBORenderCutaways') -join "`n"
if ($CutawaySignature -notmatch 'private boolean IsCutawaySquare\(zombie\.iso\.fboRenderChunk\.FBORenderCutaways\$CutawayWall, zombie\.iso\.IsoGridSquare, zombie\.iso\.IsoGridSquare, long\);') {
    throw 'Exact FBORenderCutaways.IsCutawaySquare signature was not found.'
}

if (Test-Path -LiteralPath $JarOut) {
    Remove-Item -LiteralPath $JarOut -Force
}
& $JarTool '--create' '--file' $JarOut '-C' $Classes '.'
if ($LASTEXITCODE -ne 0) { throw 'JAR creation failed.' }

$Entries = @(& $JarTool 'tf' $JarOut)
$Unexpected = @($Entries | Where-Object { $_ -like 'zombie/*' -or $_ -like 'me/zed_0xff/*' })
if ($Unexpected.Count -ne 0) {
    throw "JAR contains forbidden bundled runtime classes: $($Unexpected -join ', ')"
}

[pscustomobject]@{
    Jar = $JarOut
    SHA256 = (Get-FileHash -LiteralPath $JarOut -Algorithm SHA256).Hash
    Entries = $Entries.Count
    PzTarget = '42.21.0 revision 4a0e9546ec'
    ZombieBuddyTarget = '2.3.2'
} | Format-List
