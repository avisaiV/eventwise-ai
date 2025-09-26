package com.eventwise.reco.ai;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EventClient {

  private final RestClient http;

  public EventClient() {
    // For local dev; you can switch to config/env later if you like
    this.http = RestClient.builder()
        .baseUrl("http://localhost:8083")
        .build();
  }

  public EventDto findById(long id) {
    return http.get()
        .uri("/events/{id}", id)
        .retrieve()
        .body(EventDto.class);
  }

  public record EventDto(Long id, String title, String category, Integer capacity) {}
}
