package com.eventwise.booking.messaging;

import com.eventwise.booking.domain.Booking;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BookingProducer {
  private final StreamBridge streamBridge;

  public BookingProducer(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  public void emitBookingCreated(Booking b) {
    var payload = Map.of(
        "type", "BookingCreated",
        "userId", b.getUserId(),
        "eventId", b.getEventId(),
        "qty", b.getQty()
    );
    // send to binding name + index
    streamBridge.send("bookings-out-0", payload);
  }
}
