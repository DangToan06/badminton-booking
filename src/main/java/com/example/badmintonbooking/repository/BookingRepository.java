package com.example.badmintonbooking.repository;

import com.example.badmintonbooking.entity.Booking;
import com.example.badmintonbooking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.court.id    = :courtId
          AND b.bookingDate = :bookingDate
          AND b.timeSlot    = :timeSlot
          AND b.status IN ('PENDING', 'CONFIRMED')
        """)
    boolean existsConflictingBooking(
            @Param("courtId") Long courtId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("timeSlot") String timeSlot
    );

    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            BookingStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT b FROM Booking b
        WHERE b.court.cluster.id = :clusterId
          AND (:status IS NULL       OR b.status      = :status)
          AND (:bookingDate IS NULL  OR b.bookingDate = :bookingDate)
        ORDER BY b.bookingDate ASC, b.timeSlot ASC
        """)
    Page<Booking> findByClusterWithFilters(
            @Param("clusterId") Long clusterId,
            @Param("status") BookingStatus status,
            @Param("bookingDate") LocalDate bookingDate,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("UPDATE Booking b SET b.status = :status WHERE b.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") BookingStatus status);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.court.cluster.id = :clusterId
          AND b.status IN ('CONFIRMED', 'CHECKED_IN')
          AND YEAR(b.bookingDate)  = :year
          AND MONTH(b.bookingDate) = :month
        """)
    List<Booking> findConfirmedBookingsForRevenue(
            @Param("clusterId") Long clusterId,
            @Param("year") int year,
            @Param("month") int month
    );

    @Query("""
        SELECT b.timeSlot FROM Booking b
        WHERE b.court.id    = :courtId
          AND b.bookingDate = :bookingDate
          AND b.status IN ('PENDING', 'CONFIRMED')
        """)
    List<String> findBookedTimeSlots(
            @Param("courtId") Long courtId,
            @Param("bookingDate") LocalDate bookingDate
    );
}
