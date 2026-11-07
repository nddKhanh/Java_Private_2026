$ErrorActionPreference = "Stop"

$projectRoot = Split-Path $PSScriptRoot -Parent
Push-Location $projectRoot
try {
    & .\mvnw.cmd test
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    Push-Location (Join-Path $projectRoot "game-web")
    try {
        & npm ci
        if ($LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }
        & npm run build
        exit $LASTEXITCODE
    }
    finally {
        Pop-Location
    }
}
finally {
    Pop-Location
}
