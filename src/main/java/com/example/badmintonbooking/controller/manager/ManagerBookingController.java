package com.example.badmintonbooking.controller.manager;

import com.example.badmintonbooking.dto.response.ApiResponse;
import com.example.badmintonbooking.dto.response.BookingDTO;
import com.example.badmintonbooking.dto.response.PageResponse;
import com.example.badmintonbooking.enums.BookingStatus;
import com.example.badmintonbooking.service.IBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/manager/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ManagerBookingController {

    private final IBookingService bookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookingDTO>>> getBookingsByCluster(
            @RequestParam Long clusterId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<BookingDTO> result = bookingService.getBookingsByCluster(
                clusterId, status, date, page, size
        );
        return ResponseEntity.ok(ApiResponse.success("Success", result));
    }


    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BookingDTO>> confirmBooking(@PathVariable Long id) {
        BookingDTO booking = bookingService.confirmBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", booking));
    }


    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(@PathVariable Long id) {
        BookingDTO booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", booking));
    }


    @PatchMapping("/{id}/checkin")
    public ResponseEntity<ApiResponse<BookingDTO>> checkInBooking(@PathVariable Long id) {
        BookingDTO booking = bookingService.checkInBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Check-in successful", booking));
    }
}