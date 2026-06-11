package com.example.badmintonbooking.service.impl;

import com.example.badmintonbooking.dto.request.BookingRequest;
import com.example.badmintonbooking.dto.response.AvailableSlotResponse;
import com.example.badmintonbooking.dto.response.BookingDTO;
import com.example.badmintonbooking.dto.response.PageResponse;
import com.example.badmintonbooking.entity.Booking;
import com.example.badmintonbooking.entity.Court;
import com.example.badmintonbooking.enums.BookingStatus;
import com.example.badmintonbooking.exception.BookingConflictException;
import com.example.badmintonbooking.exception.ResourceNotFoundException;
import com.example.badmintonbooking.repository.BookingRepository;
import com.example.badmintonbooking.repository.CourtRepository;
import com.example.badmintonbooking.security.principal.UserPrincipal;
import com.example.badmintonbooking.service.IBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements IBookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;

    private static final List<String> ALL_TIME_SLOTS = Arrays.asList(
            "05:00-07:00",
            "07:00-09:00",
            "09:00-11:00",
            "11:00-13:00",
            "13:00-15:00",
            "15:00-17:00",
            "17:00-19:00",
            "19:00-21:00",
            "21:00-23:00"
    );

    private static final BigDecimal PRICE_PER_SLOT = new BigDecimal("120000");

    @Override
    @Transactional
    public BookingDTO createBooking(BookingRequest request, UserPrincipal principal) {
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> ResourceNotFoundException.of("Court", request.getCourtId()));

        if (!court.getIsAvailable()) {
            throw new RuntimeException("Court is not available");
        }

        if (!ALL_TIME_SLOTS.contains(request.getTimeSlot())) {
            throw new RuntimeException(
                    "Invalid time slot. Valid slots: " + String.join(", ", ALL_TIME_SLOTS)
            );
        }

        boolean hasConflict = bookingRepository.existsConflictingBooking(
                request.getCourtId(),
                request.getBookingDate(),
                request.getTimeSlot()
        );

        if (hasConflict) {
            throw new BookingConflictException(
                    String.format(
                            "Court '%s' is already booked on %s at %s",
                            court.getCourtName(),
                            request.getBookingDate(),
                            request.getTimeSlot()
                    )
            );
        }

        Booking booking = Booking.builder()
                .court(court)
                .user(principal.user())
                .bookingDate(request.getBookingDate())
                .timeSlot(request.getTimeSlot())
                .totalPrice(PRICE_PER_SLOT)
                .status(BookingStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);
        return BookingDTO.fromEntity(saved);
    }

    @Override
    public PageResponse<BookingDTO> getMyBookings(UserPrincipal principal, BookingStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Booking> bookingPage;

        if (status != null) {
            bookingPage = bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                    principal.user().getId(), status, pageable
            );
        } else {
            bookingPage = bookingRepository.findByUserIdOrderByCreatedAtDesc(
                    principal.user().getId(), pageable
            );
        }

        List<BookingDTO> dtos = bookingPage.getContent()
                .stream()
                .filter(b -> b != null)
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());

        Page<BookingDTO> dtoPage = new PageImpl<>(dtos, pageable, bookingPage.getTotalElements());
        return PageResponse.fromPage(dtoPage);
    }

    @Override
    public BookingDTO getMyBookingById(Long bookingId, UserPrincipal principal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

        if (!booking.getUser().getId().equals(principal.user().getId())) {
            throw new RuntimeException("You don't have permission to view this booking");
        }

        return BookingDTO.fromEntity(booking);
    }

    @Override
    public BookingDTO cancelMyBooking(Long bookingId, UserPrincipal principal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

        if (!booking.getUser().getId().equals(principal.user().getId())) {
            throw new RuntimeException("You don't have permission to cancel this booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException(
                    "Cannot cancel booking with status: " + booking.getStatus() +
                            ". Only PENDING bookings can be cancelled by customer."
            );
        }

        bookingRepository.updateStatus(bookingId, BookingStatus.CANCELED);
        log.info("Customer '{}' cancelled booking id: {}", principal.getUsername(), bookingId);

        booking.setStatus(BookingStatus.CANCELED);
        return BookingDTO.fromEntity(booking);
    }

    @Override
    public PageResponse<BookingDTO> getBookingsByCluster(Long clusterId, BookingStatus status, LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Booking> bookingPage = bookingRepository.findByClusterWithFilters(
                clusterId, status, date, pageable
        );

        List<BookingDTO> dtos = bookingPage.getContent()
                .stream()
                .filter(b -> b != null)
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());

        Page<BookingDTO> dtoPage = new PageImpl<>(dtos, pageable, bookingPage.getTotalElements());
        return PageResponse.fromPage(dtoPage);
    }

    @Override
    public BookingDTO confirmBooking(Long bookingId) {
        Booking booking = findBookingById(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException(
                    "Cannot confirm booking with status: " + booking.getStatus()
            );
        }

        bookingRepository.updateStatus(bookingId, BookingStatus.CONFIRMED);
        log.info("Booking id: {} confirmed", bookingId);

        booking.setStatus(BookingStatus.CONFIRMED);
        return BookingDTO.fromEntity(booking);
    }

    @Override
    public BookingDTO cancelBooking(Long bookingId) {
        Booking booking = findBookingById(bookingId);

        if (booking.getStatus() == BookingStatus.CHECKED_IN ||
                booking.getStatus() == BookingStatus.CANCELED) {
            throw new RuntimeException(
                    "Cannot cancel booking with status: " + booking.getStatus()
            );
        }

        bookingRepository.updateStatus(bookingId, BookingStatus.CANCELED);
        log.info("Booking id: {} canceled by admin/manager", bookingId);

        booking.setStatus(BookingStatus.CANCELED);
        return BookingDTO.fromEntity(booking);
    }

    @Override
    public BookingDTO checkInBooking(Long bookingId) {
        Booking booking = findBookingById(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException(
                    "Cannot check-in booking with status: " + booking.getStatus() +
                            ". Booking must be CONFIRMED first."
            );
        }

        bookingRepository.updateStatus(bookingId, BookingStatus.CHECKED_IN);
        log.info("Booking id: {} checked in", bookingId);

        booking.setStatus(BookingStatus.CHECKED_IN);
        return BookingDTO.fromEntity(booking);
    }

    @Override
    public AvailableSlotResponse getAvailableSlots(Long courtId, LocalDate date) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> ResourceNotFoundException.of("Court", courtId));

        // Lấy danh sách khung giờ đã bị đặt từ DB
        List<String> bookedSlots = bookingRepository.findBookedTimeSlots(courtId, date);

        List<AvailableSlotResponse.SlotInfo> slotInfos = ALL_TIME_SLOTS
                .stream()
                .map(slot -> AvailableSlotResponse.SlotInfo.builder()
                        .timeSlot(slot)
                        .available(!bookedSlots.contains(slot))  // true = còn trống
                        .build()
                )
                .collect(Collectors.toList());

        return AvailableSlotResponse.builder()
                .courtId(courtId)
                .courtName(court.getCourtName())
                .date(date.toString())
                .pricePerSlot(PRICE_PER_SLOT)
                .slots(slotInfos)
                .build();
    }

    private Booking findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", id));
    }
}
