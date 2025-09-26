package com.eventwise.reco.stream;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.Stores;   // <-- add this import
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.function.Function;

@Configuration
public class PopularityTopology {

  @Bean
  public Function<KStream<String, BookingEvent>, KTable<Long, Long>> bookingsStream() {
    Serde<Long> longSerde = Serdes.Long();
    JsonSerde<BookingEvent> bookingSerde = new JsonSerde<>(BookingEvent.class);

    return input -> input
        .filter((k, v) -> v != null && "BookingCreated".equals(v.getType())
            && v.getEventId() != null && v.getQty() != null)
        .selectKey((k, v) -> v.getEventId())
        .groupByKey(Grouped.with(longSerde, bookingSerde))
        .aggregate(
            () -> 0L,
            (eventId, evt, agg) -> agg + evt.getQty(),
            // ---- this line changed: give a store supplier explicitly ----
            Materialized.<Long, Long>as(Stores.inMemoryKeyValueStore("event-popularity"))
                .withKeySerde(longSerde)
                .withValueSerde(longSerde)
        );
  }
}
