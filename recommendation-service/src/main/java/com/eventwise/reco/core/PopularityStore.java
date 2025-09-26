package com.eventwise.reco.core;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PopularityStore {
  // eventId -> score
  private final ConcurrentHashMap<Long, Integer> popularity = new ConcurrentHashMap<>();

  public void add(long eventId, int qty) {
    popularity.merge(eventId, qty, Integer::sum);
  }

  public Map<Long, Integer> snapshot() {
    return Map.copyOf(popularity);
  }
}
