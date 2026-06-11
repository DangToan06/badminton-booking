package com.example.badmintonbooking.dto.response;

import com.example.badmintonbooking.entity.Booking;
import com.example.badmintonbooking.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class BookingDTO {

    private Long id;
    private LocalDate bookingDate;
    private String timeSlot;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;

    // Thông tin sân (flatten - không nest object phức tạp)
    private Long courtId;
    private String courtName;
    private String courtType;

    // Thông tin cụm sân
    private Long clusterId;
    private String clusterName;
    private String clusterAddress;

    // Thông tin customer (hiển thị cho Admin/Manager)
    private Long userId;
    private String username;
    private String fullName;
    private String phoneNumber;

    public static BookingDTO fromEntity(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .bookingDate(booking.getBookingDate())
                .timeSlot(booking.getTimeSlot())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                // Thông tin sân
                .courtId(booking.getCourt().getId())
                .courtName(booking.getCourt().getCourtName())
                .courtType(booking.getCourt().getType())
                // Thông tin cụm sân
                .clusterId(booking.getCourt().getCluster().getId())
                .clusterName(booking.getCourt().getCluster().getName())
                .clusterAddress(booking.getCourt().getCluster().getAddress())
                // Thông tin user
                .userId(booking.getUser().getId())
                .username(booking.getUser().getUsername())
                .fullName(booking.getUser().getFullName())
                .phoneNumber(booking.getUser().getPhoneNumber())
                .build();
    }
}
