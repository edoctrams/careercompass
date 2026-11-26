$ErrorActionPreference = "Stop"

# 1. Set JAVA_HOME to the installed JDK 26
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    if (Test-Path "C:\Program Files\Java\jdk-26.0.2\bin\java.exe") {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.2"
    } elseif (Test-Path "C:\Program Files\Java\latest\bin\java.exe") {
        $env:JAVA_HOME = "C:\Program Files\Java\latest"
    }
}

# 2. Load database credentials from .env
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $key = $parts[0].Trim()
            $val = $parts[1].Trim().Trim('"').Trim("'")
            [System.Environment]::SetEnvironmentVariable($key, $val, "Process")
        }
    }
}

# 3. Free port 8080 if a previous java instance is still holding it
$existing = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($existing) {
    foreach ($conn in $existing) {
        Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
    }
    Start-Sleep -Seconds 1
}

Write-Host "Starting Career Compass on http://localhost:8080 ..." -ForegroundColor Cyan
.\mvnw.cmd -o spring-boot:run
