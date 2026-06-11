package com.example.badmintonbooking.service;

import com.example.badmintonbooking.dto.request.BookingRequest;
import com.example.badmintonbooking.dto.response.AvailableSlotResponse;
import com.example.badmintonbooking.dto.response.BookingDTO;
import com.example.badmintonbooking.dto.response.PageResponse;
import com.example.badmintonbooking.enums.BookingStatus;
import com.example.badmintonbooking.security.principal.UserPrincipal;

import java.time.LocalDate;

public interface IBookingService {
    BookingDTO createBooking(BookingRequest request, UserPrincipal principal);

    PageResponse<BookingDTO> getMyBookings(UserPrincipal principal, BookingStatus status, int page, int size);

    BookingDTO getMyBookingById(Long bookingId, UserPrincipal principal);

    BookingDTO cancelMyBooking(Long bookingId, UserPrincipal principal);

    PageResponse<BookingDTO> getBookingsByCluster(Long clusterId, BookingStatus status, LocalDate date, int page, int size);

    BookingDTO confirmBooking(Long bookingId);

    BookingDTO cancelBooking(Long bookingId);

    BookingDTO checkInBooking(Long bookingId);

    AvailableSlotResponse getAvailableSlots(Long courtId, LocalDate date);


}
