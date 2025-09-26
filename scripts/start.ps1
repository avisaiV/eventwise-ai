# Ensure we fail fast
$ErrorActionPreference = "Stop"

# 1) Redpanda up (Kafka)
docker compose up -d redpanda

# Wait a moment for broker to be ready
Start-Sleep -Seconds 3

# 2) Run the three services in separate windows
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$repo = Resolve-Path "$root\.."

function Run-Mod {
  param([string]$module)
  Push-Location "$repo\$module"
  Start-Process pwsh -ArgumentList "-NoExit","-Command","mvn -q spring-boot:run"
  Pop-Location
}

Run-Mod "event-service"
Run-Mod "booking-service"
Run-Mod "recommendation-service"

Write-Host "`nStarted: redpanda + 3 services. Endpoints:"
Write-Host "  event-service            http://localhost:8083"
Write-Host "  booking-service          http://localhost:8081"
Write-Host "  recommendation-service   http://localhost:8084"
