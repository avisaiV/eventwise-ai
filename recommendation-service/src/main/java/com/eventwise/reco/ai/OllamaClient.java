package com.eventwise.reco.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;

@Component
public class OllamaClient {

  private final RestClient http;
  private final String model;

  public OllamaClient(
      @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
      @Value("${ollama.model:llama3.2}") String model) {
    this.http = RestClient.builder().baseUrl(baseUrl).build();
    this.model = model;
  }

  public String chat(String system, String user) {
    Map<String, Object> body = Map.of(
        "model", model,
        "messages", List.of(
            Map.of("role", "system", "content", system),
            Map.of("role", "user", "content", user)
        ),
        "stream", false
    );

    ChatResponse res = http.post()
        .uri("/api/chat")
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(ChatResponse.class);

    if (res == null || res.message() == null || res.message().content() == null) {
      return "(no response)";
    }
    return res.message().content().trim();
  }

  // DTOs that match Ollama /api/chat response
  public record ChatResponse(Message message) {}
  public record Message(String role, String content) {}
}
