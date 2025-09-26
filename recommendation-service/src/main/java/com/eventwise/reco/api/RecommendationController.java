package com.eventwise.reco.api;

import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

  private final InteractiveQueryService iq;

  public RecommendationController(InteractiveQueryService iq) {
    this.iq = iq;
  }

  @GetMapping
  public List<Map<String, Object>> top(@RequestParam(name = "limit", defaultValue = "5") int limit) {
    ReadOnlyKeyValueStore<Long, Long> store =
        iq.getQueryableStore("event-popularity", QueryableStoreTypes.keyValueStore());

    List<Map<String, Object>> list = new ArrayList<>();
    try (var iter = store.all()) {
      while (iter.hasNext()) {
        var kv = iter.next();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("eventId", kv.key);
        m.put("score", kv.value);
        list.add(m);
      }
    }
    list.sort((a, b) -> Long.compare(((Number)b.get("score")).longValue(),
                                     ((Number)a.get("score")).longValue()));
    return list.stream().limit(limit).toList();
  }
}
