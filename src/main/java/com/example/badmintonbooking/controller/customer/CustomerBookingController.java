package com.example.badmintonbooking.controller.customer;

import com.example.badmintonbooking.dto.request.BookingRequest;
import com.example.badmintonbooking.dto.response.ApiResponse;
import com.example.badmintonbooking.dto.response.AvailableSlotResponse;
import com.example.badmintonbooking.dto.response.BookingDTO;
import com.example.badmintonbooking.dto.response.PageResponse;
import com.example.badmintonbooking.enums.BookingStatus;
import com.example.badmintonbooking.security.principal.UserPrincipal;
import com.example.badmintonbooking.service.IBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerBookingController {

    private final IBookingService bookingService;

    @GetMapping("/api/v1/courts/{courtId}/slots")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<AvailableSlotResponse>> getAvailableSlots(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        AvailableSlotResponse response = bookingService.getAvailableSlots(courtId, date);
        return ResponseEntity.ok(ApiResponse.success("Success", response));
    }

    @PostMapping("/api/v1/customer/bookings")
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        BookingDTO booking =  bookingService.createBooking(request, userPrincipal);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", booking));
    }

    @GetMapping("/api/v1/customer/bookings")
    public ResponseEntity<ApiResponse<PageResponse<BookingDTO>>> getMyBooking(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        PageResponse<BookingDTO> res = bookingService.getMyBookings(userPrincipal, status, page, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Success", res));
    }

    @GetMapping("/api/v1/customer/bookings/{id}")
    public ResponseEntity<ApiResponse<BookingDTO>> getMyBookingById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        BookingDTO booking = bookingService.getMyBookingById(id, userPrincipal);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Success", booking));
    }


    @PatchMapping("/api/v1/customer/bookings/{id}/cancle")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        BookingDTO booking = bookingService.cancelMyBooking(id, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", booking));
    }
}
