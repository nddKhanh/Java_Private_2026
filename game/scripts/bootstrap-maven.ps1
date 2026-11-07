$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$mavenVersion = '3.9.9'
$mavenHome = Join-Path $root '.mvn\apache-maven'
$archive = Join-Path $root '.mvn\maven.zip'
$url = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mavenVersion/apache-maven-$mavenVersion-bin.zip"

foreach ($candidate in @(
    (Get-Command mvn.cmd -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source -First 1),
    'C:\Program Files\Apache\maven\bin\mvn.cmd'
)) {
    if ($candidate -and (Test-Path -LiteralPath $candidate)) {
        & $candidate @args
        exit $LASTEXITCODE
    }
}

if (-not (Test-Path (Join-Path $mavenHome 'bin\mvn.cmd'))) {
    New-Item -ItemType Directory -Path (Join-Path $root '.mvn') -Force | Out-Null
    Invoke-WebRequest -Uri $url -OutFile $archive
    Expand-Archive -LiteralPath $archive -DestinationPath (Join-Path $root '.mvn') -Force
    $extracted = Join-Path $root ".mvn\apache-maven-$mavenVersion"
    Move-Item -LiteralPath $extracted -Destination $mavenHome -Force
}

& (Join-Path $mavenHome 'bin\mvn.cmd') @args
exit $LASTEXITCODE
