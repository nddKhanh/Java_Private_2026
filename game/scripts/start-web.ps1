$ErrorActionPreference = "Stop"

$projectRoot = Split-Path $PSScriptRoot -Parent
$webRoot = Join-Path $projectRoot "game-web"
Push-Location $webRoot
try {
    & npm ci
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    & npm run dev -- --host 127.0.0.1
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
