package com.eventwise.booking.api;

import com.eventwise.booking.domain.Booking;
import com.eventwise.booking.messaging.BookingProducer;
import com.eventwise.booking.repo.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingRepository repo;
    private final BookingProducer producer;
    private final EventClient events;

    @Autowired
    public BookingController(BookingRepository repo,
                             BookingProducer producer,
                             EventClient events) {
        this.repo = repo;
        this.producer = producer;
        this.events = events;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BookingRequest req) {
        // fetch event and validate capacity
        var ev = events.findById(req.eventId());
        if (ev == null || ev.capacity() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorBody("event not found"));
        }

        long already = repo.totalQtyByEventId(req.eventId());
        long requested = req.qty();
        if (already + requested > ev.capacity()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorBody("capacity exceeded: " + (already + requested) + " > " + ev.capacity()));
        }

        // save booking
        var booking = Booking.builder()
            .userId(req.userId())
            .eventId(req.eventId())
            .qty(req.qty())
            .status("CREATED")
            .build();

        var saved = repo.save(booking);

        // emit event
        producer.emitBookingCreated(saved);

        return ResponseEntity.ok(new CreateResponse(saved.getId(), "booking created", true));
    }

      @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") long id) {
      var opt = repo.findById(id);
      if (opt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorBody("booking " + id + " not found"));
      }
      var b = opt.get();
      return ResponseEntity.ok(
          new BookingResponse(b.getId(), b.getUserId(), b.getEventId(), b.getQty(), b.getStatus())
      );
    }


    // --- Request/Response records ---
    public record BookingRequest(long userId, long eventId, long qty) {}
    public record CreateResponse(long id, String message, boolean eventEmitted) {}
    public record BookingResponse(long id, long userId, long eventId, long qty, String status) {}
    public record ErrorBody(String message) {}
}
