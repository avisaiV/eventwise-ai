EventWise — End-to-End Demo (Windows)

EventWise is a tiny event platform showing:

- CRUD microservices (Events, Bookings)
- Event-driven messaging (Kafka via Redpanda)
- Real-time stream processing (Spring Cloud Stream + Kafka Streams)
- Human-readable explanations (rule-based or local LLM via Ollama, free)

1) Prerequisites (Windows)

Use winget (or install manually).
Do not run these if you already have them installed.

# Java 21 (Temurin)
winget install -e --id EclipseAdoptium.Temurin.21.JDK

# Maven
winget install -e --id Apache.Maven

# Docker Desktop
winget install -e --id Docker.DockerDesktop

# Postman (optional if you prefer over PowerShell)
winget install -e --id Postman.Postman

# Ollama (local LLM runtime)
winget install -e --id Ollama.Ollama


Verify:

java -version
mvn -v
docker --version
ollama --version


If Docker complains about virtualization/WSL: enable CPU virtualization in BIOS, turn on Virtual Machine Platform and Windows Hypervisor Platform in “Turn Windows features on or off”, install WSL2, then reboot and start Docker Desktop.

2) Clone and build
git clone https://github.com/avisaiV/eventwise-ai.git
cd eventwise-ai

# one-time build (downloads dependencies)
mvn -DskipTests clean package

3) First-time LLM setup (local & free)
# Start Ollama background server (if it’s not already running)
ollama serve   # if you see “port already in use”, it’s already running

# Pull a small general model (~2 GB)
ollama pull llama3.2

4) Start infrastructure (Kafka/Redpanda)

From repo root:

docker compose up -d


Check:

docker ps  # expect redpandadata/redpanda ... ports 9092,19092

5) Run the microservices (open 3 terminals)

Optional: make JSON text look nice on Windows:

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8


Terminal A — event-service (8083)

cd eventwise-ai\event-service
mvn -q -DskipTests spring-boot:run


Terminal B — booking-service (8081)

cd eventwise-ai\booking-service
mvn -q -DskipTests spring-boot:run

Terminal C — recommendation-service (8084)

cd eventwise-ai\recommendation-service

Set env vars in this terminal before starting up the service:

$env:EXPLAINER_MODE = 'llm'              # use local LLM
$env:OLLAMA_BASE_URL = 'http://localhost:11434'
$env:OLLAMA_MODEL = 'llama3.2'

mvn -q -DskipTests spring-boot:run

6) Quick end-to-end demo (copy/paste)

A) Seed events

$e1 = Invoke-RestMethod -Method Post http://localhost:8083/events `
  -ContentType application/json `
  -Body (@{ title="UOW Tech Fest"; category="Tech"; capacity=100 } | ConvertTo-Json)

$e2 = Invoke-RestMethod -Method Post http://localhost:8083/events `
  -ContentType application/json `
  -Body (@{ title="AI Summit"; category="Tech"; capacity=5 } | ConvertTo-Json)

B) Create bookings (capacity guard blocks the last one)

Invoke-RestMethod -Method Post http://localhost:8081/bookings `
  -ContentType application/json `
  -Body (@{ userId=1; eventId=$e1.id; qty=3 } | ConvertTo-Json) | Out-Host

Invoke-RestMethod -Method Post http://localhost:8081/bookings `
  -ContentType application/json `
  -Body (@{ userId=2; eventId=$e2.id; qty=4 } | ConvertTo-Json) | Out-Host

# Exceeds capacity => expect HTTP 409
try {
  Invoke-RestMethod -Method Post http://localhost:8081/bookings `
    -ContentType application/json `
    -Body (@{ userId=3; eventId=$e2.id; qty=2 } | ConvertTo-Json)
} catch { $_.Exception.Response.StatusCode.value__ }

C) Real-time recommendations + explanations

# Top-N by running popularity score (Kafka Streams state store)
Invoke-RestMethod "http://localhost:8084/recommendations?limit=5" | ConvertTo-Json

# Human-readable explanation (LLM if enabled, else rule-based)
Invoke-RestMethod "http://localhost:8084/explanations?eventId=$($e1.id)" | ConvertTo-Json

D) CRUD sanity checks
# Fetch a single booking (replace 1 with the ID you saw)
Invoke-RestMethod "http://localhost:8081/bookings/1" | ConvertTo-Json

# Fetch a single event
Invoke-RestMethod "http://localhost:8083/events/$($e1.id)" | ConvertTo-Json

7) Stop / reset
# stop services (Ctrl+C in each service terminal)
# stop Kafka/Redpanda
docker compose down

8) Troubleshooting (fast)

Docker won’t start / virtualization → enable CPU virtualization; turn on Virtual Machine Platform + Windows Hypervisor Platform; install WSL2; reboot.

Ollama “port in use” → it’s already running (good).

LLM explanations look generic or time out → confirm the model is present:
curl http://localhost:11434/api/tags shows llama3.2; re-export the three env vars and restart recommendation-service.

Recommendations empty → create bookings first; the KTable only updates from the bookings topic.