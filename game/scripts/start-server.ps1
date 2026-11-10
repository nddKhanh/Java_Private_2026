param(
    [switch]$Fresh
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path $PSScriptRoot -Parent
$previousDatabaseUrl = $env:GAME_DB_URL
$hadDatabaseUrl = Test-Path Env:GAME_DB_URL
Push-Location $projectRoot
try {
    if ($Fresh) {
        $env:GAME_DB_URL = "jdbc:h2:mem:ant-colony-wars"
    }
    & .\mvnw.cmd -pl game-server -am install -DskipTests
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    Push-Location (Join-Path $projectRoot "game-server")
    try {
        & ..\mvnw.cmd spring-boot:run
        exit $LASTEXITCODE
    }
    finally {
        Pop-Location
    }
}
finally {
    Pop-Location
    if ($hadDatabaseUrl) {
        $env:GAME_DB_URL = $previousDatabaseUrl
    }
    else {
        Remove-Item Env:GAME_DB_URL -ErrorAction SilentlyContinue
    }
}
