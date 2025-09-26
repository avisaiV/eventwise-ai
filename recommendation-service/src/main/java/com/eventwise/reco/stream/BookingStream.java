package com.eventwise.reco.stream;

import com.eventwise.reco.core.PopularityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Consumer;

@Configuration
public class BookingStream {

  private static final Logger log = LoggerFactory.getLogger(BookingStream.class);

  @Bean
  public Consumer<Map<String, Object>> bookingsIn(PopularityStore store) {
    return payload -> {
      log.info("Received payload: {}", payload);
      if (payload == null) return;
      if (!"BookingCreated".equals(payload.get("type"))) return;
      Object ev = payload.get("eventId");
      Object q  = payload.get("qty");
      if (ev == null || q == null) return;
      long eventId = Long.parseLong(ev.toString());
      int qty      = Integer.parseInt(q.toString());
      store.add(eventId, qty);
      log.info("Updated popularity: eventId={} +{}", eventId, qty);
    };
  }
}
