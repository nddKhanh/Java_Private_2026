$ErrorActionPreference = "Stop"

$projectRoot = Split-Path $PSScriptRoot -Parent
Push-Location $projectRoot
try {
    & .\mvnw.cmd -DskipTests install
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    & .\mvnw.cmd -f game-desktop\pom.xml javafx:run
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
