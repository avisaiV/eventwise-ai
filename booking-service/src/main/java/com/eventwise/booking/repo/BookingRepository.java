package com.eventwise.booking.repo;

import com.eventwise.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select coalesce(sum(b.qty), 0) from Booking b where b.eventId = :eventId")
    long totalQtyByEventId(@Param("eventId") long eventId);
}

