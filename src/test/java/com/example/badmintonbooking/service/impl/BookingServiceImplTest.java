package com.example.badmintonbooking.service.impl;

import com.example.badmintonbooking.dto.request.BookingRequest;
import com.example.badmintonbooking.dto.response.BookingDTO;
import com.example.badmintonbooking.entity.BadmintonCluster;
import com.example.badmintonbooking.entity.Booking;
import com.example.badmintonbooking.entity.Court;
import com.example.badmintonbooking.entity.User;
import com.example.badmintonbooking.enums.BookingStatus;
import com.example.badmintonbooking.enums.Role;
import com.example.badmintonbooking.exception.BookingConflictException;
import com.example.badmintonbooking.exception.CustomExceptions;
import com.example.badmintonbooking.repository.BookingRepository;
import com.example.badmintonbooking.repository.CourtRepository;
import com.example.badmintonbooking.security.principal.UserPrincipal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    // Dữ liệu mẫu dùng chung
    private Court mockCourt;
    private User          mockUser;
    private UserPrincipal mockPrincipal;
    private BookingRequest validRequest;

    @BeforeEach
    void setUp() {
        // Tạo User mẫu
        mockUser = User.builder()
                .id(1L)
                .username("customer1")
                .fullName("Khách Hàng")
                .role(Role.CUSTOMER)
                .isEnabled(true)
                .build();

        // Tạo BadmintonCluster mẫu
        BadmintonCluster cluster = BadmintonCluster.builder()
                .id(1L)
                .name("Cụm sân Quận 1")
                .address("123 Nguyễn Huệ, Q1")
                .build();

        // Tạo Court mẫu
        mockCourt = Court.builder()
                .id(1L)
                .courtName("Sân A1")
                .type("Tiêu chuẩn")
                .isAvailable(true)
                .cluster(cluster)
                .build();

        mockPrincipal = new UserPrincipal(mockUser);

        // Request đặt sân hợp lệ
        validRequest = new BookingRequest();
        validRequest.setCourtId(1L);
        validRequest.setBookingDate(LocalDate.now().plusDays(1));
        validRequest.setTimeSlot("07:00-09:00");
    }

    // =====================================================================
    // Test 1: Đặt sân thành công
    // =====================================================================

    @Test
    @DisplayName("Test 1: Đặt sân thành công → trả về BookingDTO với status PENDING")
    void createBooking_Success_ReturnBookingDTO() {
        // GIVEN - Chuẩn bị mock
        when(courtRepository.findById(1L)).thenReturn(Optional.of(mockCourt));
        when(bookingRepository.existsConflictingBooking(anyLong(), any(), anyString()))
                .thenReturn(false); // Không trùng lịch

        Booking savedBooking = Booking.builder()
                .id(1L)
                .court(mockCourt)
                .user(mockUser)
                .bookingDate(validRequest.getBookingDate())
                .timeSlot(validRequest.getTimeSlot())
                .totalPrice(new BigDecimal("120000"))
                .status(BookingStatus.PENDING)
                .build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // WHEN - Thực thi
        BookingDTO result = bookingService.createBooking(validRequest, mockPrincipal);

        // THEN - Kiểm tra kết quả
        assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(1L);
        Assertions.assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        Assertions.assertThat(result.getCourtName()).isEqualTo("Sân A1");
        Assertions.assertThat(result.getTimeSlot()).isEqualTo("07:00-09:00");

        // Verify repository được gọi đúng
        verify(courtRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }



    // =====================================================================
    // Test 2: Đặt sân thất bại - Sân không tồn tại → 404
    // =====================================================================

    @Test
    @DisplayName("Test 2: Sân không tồn tại → ném CourtNotFoundException (404)")
    void createBooking_CourtNotFound_ThrowException() {
        // GIVEN
        when(courtRepository.findById(999L)).thenReturn(Optional.empty());
        validRequest.setCourtId(999L);

        // WHEN + THEN
        assertThatThrownBy(() ->
                bookingService.createBooking(validRequest, mockPrincipal)
        )
                .isInstanceOf(com.example.badmintonbooking.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Court not found with id: 999");

        // Verify KHÔNG gọi save vì đã throw trước đó
        verify(bookingRepository, never()).save(any());
    }

    // =====================================================================
    // Test 3: Đặt sân thất bại - Sân đang bảo trì → 400
    // =====================================================================

    @Test
    @DisplayName("Test 3: Sân đang bảo trì (isAvailable=false) → ném CourtNotAvailableException (400)")
    void createBooking_CourtNotAvailable_ThrowException() {
        // GIVEN - Sân đang tắt
        mockCourt.setIsAvailable(false);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(mockCourt));

        // WHEN + THEN
        assertThatThrownBy(() ->
                bookingService.createBooking(validRequest, mockPrincipal)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Court is not available");

        verify(bookingRepository, never()).save(any());
    }

    // =====================================================================
    // Test 4: Đặt sân thất bại - Trùng lịch → 409
    // =====================================================================

    @Test
    @DisplayName("Test 4: Trùng khung giờ → ném BookingConflictException (409)")
    void createBooking_ConflictingBooking_ThrowException() {
        // GIVEN - Đã có người đặt cùng giờ
        when(courtRepository.findById(1L)).thenReturn(Optional.of(mockCourt));
        when(bookingRepository.existsConflictingBooking(anyLong(), any(), anyString()))
                .thenReturn(true);

        // WHEN + THEN
        assertThatThrownBy(() ->
                bookingService.createBooking(validRequest, mockPrincipal)
        )
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("already booked");

        verify(bookingRepository, never()).save(any());
    }

    // =====================================================================
    // Test 5: Đặt sân thất bại - Khung giờ không hợp lệ → 400
    // =====================================================================

    @Test
    @DisplayName("Test 5: Khung giờ không hợp lệ → ném InvalidTimeSlotException (400)")
    void createBooking_InvalidTimeSlot_ThrowException() {
        // GIVEN - Khung giờ không nằm trong danh sách hợp lệ
        when(courtRepository.findById(1L)).thenReturn(Optional.of(mockCourt));
        validRequest.setTimeSlot("00:00-01:00"); // Không hợp lệ

        // WHEN + THEN
        assertThatThrownBy(() ->
                bookingService.createBooking(validRequest, mockPrincipal)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid time slot");

        verify(bookingRepository, never()).save(any());
    }
}