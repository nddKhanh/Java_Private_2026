$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Split-Path $PSScriptRoot -Parent)).Path
$desktopTarget = Join-Path $projectRoot "game-desktop\target"
$inputDirectory = Join-Path $desktopTarget "jpackage-input"
$packageDirectory = Join-Path $desktopTarget "package"
$expectedPrefix = $desktopTarget.TrimEnd([System.IO.Path]::DirectorySeparatorChar) + [System.IO.Path]::DirectorySeparatorChar

foreach ($directory in @($inputDirectory, $packageDirectory)) {
    $fullPath = [System.IO.Path]::GetFullPath($directory)
    if (-not $fullPath.StartsWith($expectedPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to clean a directory outside game-desktop/target: $fullPath"
    }
    if (Test-Path -LiteralPath $fullPath) {
        Remove-Item -LiteralPath $fullPath -Recurse -Force
    }
    New-Item -ItemType Directory -Path $fullPath | Out-Null
}

Push-Location $projectRoot
try {
    & .\mvnw.cmd -DskipTests install
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    & .\mvnw.cmd -f game-desktop\pom.xml dependency:copy-dependencies `
        "-DincludeScope=runtime" `
        "-DoutputDirectory=$inputDirectory"
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    $desktopJar = Join-Path $desktopTarget "game-desktop-0.1.0-SNAPSHOT.jar"
    Copy-Item -LiteralPath $desktopJar -Destination $inputDirectory

    $javaHomes = @()
    if ($env:JAVA_HOME) {
        $javaHomes += $env:JAVA_HOME
    }
    foreach ($line in (& java -XshowSettings:properties -version 2>&1)) {
        if ($line -match '^\s*java\.home\s*=\s*(.+)$') {
            $javaHomes += $Matches[1].Trim()
        }
    }
    $jpackage = $javaHomes `
        | ForEach-Object { Join-Path $_ "bin\jpackage.exe" } `
        | Where-Object { Test-Path -LiteralPath $_ } `
        | Select-Object -First 1
    if (-not $jpackage) {
        throw "jpackage.exe was not found. Install JDK 21 or set JAVA_HOME."
    }

    $arguments = @(
        "--type", "app-image",
        "--dest", $packageDirectory,
        "--input", $inputDirectory,
        "--name", "AntColonyCodeStudio",
        "--main-jar", "game-desktop-0.1.0-SNAPSHOT.jar",
        "--main-class", "com.antcolonywars.desktop.CodeStudioLauncher",
        "--add-modules", "jdk.compiler,jdk.httpserver",
        "--jlink-options", "--strip-debug --no-man-pages --no-header-files",
        "--description", "Local Java bot editor for Ant Colony Wars",
        "--vendor", "Ant Colony Wars"
    )
    & $jpackage @arguments
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    $appImage = Join-Path $packageDirectory "AntColonyCodeStudio"
    $launcher = Join-Path $appImage "AntColonyCodeStudio.exe"
    $childJava = Join-Path $appImage "runtime\bin\java.exe"
    if (-not (Test-Path -LiteralPath $launcher) -or -not (Test-Path -LiteralPath $childJava)) {
        throw "The app image is missing its launcher or child JVM executable."
    }

    $appClasspath = Join-Path $appImage "app\*"
    & $childJava -cp $appClasspath com.antcolonywars.desktop.PackageSmokeMain
    if ($LASTEXITCODE -ne 0) {
        throw "The packaged compiler smoke test failed."
    }

    Write-Host "Desktop app image: $appImage"
    exit 0
}
finally {
    Pop-Location
}
