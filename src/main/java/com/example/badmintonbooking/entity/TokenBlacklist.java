package com.example.badmintonbooking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "token_blacklists",
        indexes = @Index(
                name = "idx_token",
                columnList = "token",
                unique = true
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chuỗi JWT đã bị thu hồi (lưu toàn bộ chuỗi token)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    // Thời điểm token hết hạn (lấy từ Expiration Claim của JWT)
    // Dùng để dọn dẹp bảng tránh phình to không cần thiết
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}