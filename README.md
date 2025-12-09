1) EventWise — End-to-End Demo (Windows)

EventWise is a tiny event platform showing:

- CRUD microservices (Events, Bookings)
- Event-driven messaging (Kafka via Redpanda)
- Real-time stream processing (Spring Cloud Stream + Kafka Streams)
- Human-readable explanations (rule-based or local LLM via Ollama, free)

2) Prerequisites (Windows)

Use winget (or install manually if you prefer).

# Java 21 (Temurin)
winget install -e --id EclipseAdoptium.Temurin.21.JDK

# Maven
winget install -e --id Apache.Maven

# Docker Desktop
winget install -e --id Docker.DockerDesktop

# Ollama (local LLM runtime; optional but recommended for the demo)
winget install -e --id Ollama.Ollama

3) Verify 

java -version
mvn -v
docker --version
ollama --version 

4) First time LLM setup

# Start Ollama background server (if it’s not already running)
ollama serve
# (If you see “port already in use”, it’s already running—no problem.)

# Pull a small general model once (~2 GB)
ollama pull llama3.2

Set env vars in the same terminal you’ll use to start the apps:

$env:EXPLAINER_MODE = 'llm'              # use local LLM
$env:OLLAMA_BASE_URL = 'http://localhost:11434'
$env:OLLAMA_MODEL = 'llama3.2'

It is critical you set these before running scripts 

5) Build

From repo root:

mvn -DskipTests clean package

6) Run / Reset / Stop

We provide scripts that spin up Redpanda and run the three services.

Run this for formatting before doing anything

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# From repo root
.\scripts\start.ps1     # starts Redpanda + opens 3 windows for services

Do your demo

.\scripts\reset.ps1     # wipes Kafka topics back to clean (keeps services up)
.\scripts\stop.ps1      # stops services and broker

7) Demo

5) Quick End-to-End Demo (copy/paste)

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

# Human-readable explanation (LLM or rule-based)
Invoke-RestMethod "http://localhost:8084/explanations?eventId=$($e1.id)" | ConvertTo-Json

D) CRUD sanity checks
# Fetch a single booking
# (Replace 1 with the ID you saw returned from create)
Invoke-RestMethod "http://localhost:8081/bookings/1" | ConvertTo-Json

# Fetch a single event
Invoke-RestMethod "http://localhost:8083/events/$($e1.id)" | ConvertTo-Json
