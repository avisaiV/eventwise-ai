Write-Host "Stopping Spring Boot apps…"
# Kill typical spring-boot:run java processes
Get-Process java -ErrorAction SilentlyContinue | Where-Object {
  $_.Path -match "java" 
} | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "Stopping Redpanda…"
docker compose down
