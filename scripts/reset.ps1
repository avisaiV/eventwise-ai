$ErrorActionPreference = "Continue"

# Clear app state (if you ever add local DBs, wipe them here)

# Reset Kafka topics used in the demo
$cid = (docker ps --format "{{.ID}}\t{{.Names}}" | Select-String "redpanda" | ForEach-Object {
  ($_ -split "`t")[0]
})

if (-not $cid) {
  Write-Host "Redpanda not running; starting it…"
  docker compose up -d redpanda
  Start-Sleep -Seconds 3
  $cid = (docker ps --format "{{.ID}}\t{{.Names}}" | Select-String "redpanda" | ForEach-Object { ($_ -split "`t")[0] })
}

function Drop-Topic($name) {
  docker exec $cid rpk topic delete $name 2>$null | Out-Null
  docker exec $cid rpk topic create $name 2>$null | Out-Null
  Write-Host "reset topic: $name"
}

Drop-Topic "bookings"
Drop-Topic "recommendation-service-event-popularity-changelog"
Drop-Topic "recommendation-service-event-popularity-repartition"

Write-Host "Kafka topics reset."
