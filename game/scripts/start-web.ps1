param(
    [string]$BindAddress = "0.0.0.0"
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path $PSScriptRoot -Parent
$webRoot = Join-Path $projectRoot "game-web"
Push-Location $webRoot
try {
    & npm ci
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    & npm run dev -- --host $BindAddress
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
