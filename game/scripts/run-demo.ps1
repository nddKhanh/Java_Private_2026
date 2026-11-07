param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("BlueBot", "RedBot", "WinningScenario", "ReinforcementBot")]
    [string]$Scenario
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path $PSScriptRoot -Parent
$mainClass = "com.antcolonywars.demo.$Scenario"
Push-Location $projectRoot
try {
    & .\mvnw.cmd -pl game-demo-bots -am install -DskipTests
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    & .\mvnw.cmd -f game-demo-bots\pom.xml exec:java "-Dexec.mainClass=$mainClass"
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
