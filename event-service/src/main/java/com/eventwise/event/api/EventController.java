package com.eventwise.event.api;

import com.eventwise.event.domain.Event;
import com.eventwise.event.repo.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventController {
  private final EventRepository repo;
  private final KafkaTemplate<String, Object> kafka;
  private static final String TOPIC = "events";

  public EventController(EventRepository repo, KafkaTemplate<String, Object> kafka) {
    this.repo = repo; this.kafka = kafka;
  }

  // Use a DTO with setters so Jackson can bind
  public static class CreateEvent {
    private String title;
    private String category;
    private int capacity;
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
  }

  @PostMapping
  public ResponseEntity<Event> create(@RequestBody CreateEvent req) {
    var saved = repo.save(Event.builder()
        .title(req.getTitle())
        .category(req.getCategory())
        .capacity(req.getCapacity())
        .build());
    kafka.send(TOPIC, String.valueOf(saved.getId()), Map.of(
        "type", "EventCreated",
        "eventId", saved.getId(),
        "category", saved.getCategory(),
        "capacity", saved.getCapacity()
    ));
    return ResponseEntity.ok(saved);
  }

  @GetMapping("/{id}")
public ResponseEntity<Event> getById(@PathVariable("id") long id) {
    return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }
}
