[CmdletBinding()]
param(
    [string]$PzDir = "${env:ProgramFiles(x86)}\Steam\steamapps\common\ProjectZomboid"
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$PatchJar = Join-Path $ProjectRoot 'dist\PlayerBuiltRoomFix.jar'
$StubClasses = Join-Path $ProjectRoot 'build\stub-classes'
$Classes = Join-Path $ProjectRoot 'build\classes'
$IntegrationClasses = Join-Path $ProjectRoot 'build\integration-classes'
$Java = Join-Path $PzDir 'jre64\bin\java.exe'
$NativeAgent = Join-Path $PzDir 'zbNative.dll'

foreach ($required in @($PatchJar, $StubClasses, $Classes, $IntegrationClasses, $Java, $NativeAgent)) {
    if (-not (Test-Path -LiteralPath $required)) {
        throw "Required integration-test input not found: $required. Run build.ps1 first."
    }
}

$Classpath = @(
    $StubClasses,
    $Classes,
    $IntegrationClasses,
    (Join-Path $PzDir '*')
) -join ';'

Push-Location $PzDir
try {
    $RelativePatchJar = (Resolve-Path -LiteralPath $PatchJar -Relative).Replace('\', '/')
    $AgentArgument = "-agentpath:$NativeAgent=verbosity=0,patches_jar=$RelativePatchJar`:io.shaun.playerbuiltroomfix"
    & $Java $AgentArgument '-Djava.awt.headless=true' '-classpath' $Classpath `
        'io.shaun.playerbuiltroomfix.ZombieBuddyIntegrationTest'
    if ($LASTEXITCODE -ne 0) {
        throw "ZombieBuddy integration test failed with exit code $LASTEXITCODE."
    }

    $ActualClasspath = @(
        $IntegrationClasses,
        (Join-Path $PzDir '*')
    ) -join ';'
    & $Java $AgentArgument '-Djava.awt.headless=true' '-classpath' $ActualClasspath `
        'io.shaun.playerbuiltroomfix.ActualGameClassLoadSmokeTest'
    if ($LASTEXITCODE -ne 0) {
        throw "Actual 42.21.0 class-load smoke test failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}
