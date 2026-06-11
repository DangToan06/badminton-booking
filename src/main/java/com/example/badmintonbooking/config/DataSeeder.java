package com.example.badmintonbooking.config;

import com.example.badmintonbooking.entity.BadmintonCluster;
import com.example.badmintonbooking.entity.Court;
import com.example.badmintonbooking.entity.User;
import com.example.badmintonbooking.enums.Role;
import com.example.badmintonbooking.repository.BadmintonClusterRepository;
import com.example.badmintonbooking.repository.CourtRepository;
import com.example.badmintonbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dữ liệu mẫu gồm:
 *  - 1 tài khoản Admin
 *  - 2 tài khoản Manager (mỗi người quản lý 1 cụm sân)
 *  - 1 tài khoản Customer
 *  - 2 BadmintonCluster
 *  - 6 Court (3 sân mỗi cụm, đa dạng loại)
 *
 * Chỉ tạo dữ liệu khi bảng users CHƯA có dữ liệu (idempotent).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository            userRepository;
    private final BadmintonClusterRepository clusterRepository;
    private final CourtRepository           courtRepository;
    private final PasswordEncoder           passwordEncoder;

    // Mật khẩu mẫu cho tất cả tài khoản (đổi trước khi production)
    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // Guard: chỉ seed khi DB còn trống
        if (userRepository.count() > 0) {
            log.info("DataSeeder: Database already has data, skipping seed.");
            return;
        }

        log.info("DataSeeder: Seeding sample data...");

        // ── Bước 1: Tạo Users ────────────────────────────────────────────
        User admin    = createAdmin();
        User manager1 = createManager1();
        User manager2 = createManager2();
        User customer = createCustomer();

        // ── Bước 2: Tạo BadmintonClusters ────────────────────────────────
        BadmintonCluster cluster1 = createCluster1(manager1);
        BadmintonCluster cluster2 = createCluster2(manager2);

        // ── Bước 3: Tạo Courts (3 sân mỗi cụm) ──────────────────────────
        createCourtsForCluster1(cluster1);
        createCourtsForCluster2(cluster2);

        log.info("DataSeeder: Seeding completed successfully!");
        log.info("DataSeeder: Accounts created:");
        log.info("  ADMIN    → username: toan      | password: {}", DEFAULT_PASSWORD);
        log.info("  MANAGER  → username: duong   | password: {}", DEFAULT_PASSWORD);
        log.info("  MANAGER  → username: quang   | password: {}", DEFAULT_PASSWORD);
        log.info("  CUSTOMER → username: an  | password: {}", DEFAULT_PASSWORD);
    }

    // =====================================================================
    // USERS
    // =====================================================================

    private User createAdmin() {
        User admin = User.builder()
                .username("toan")
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .fullName("Đặng Quốc Toàn")
                .email("toandq@gamil.com")
                .phoneNumber("0901234567")
                .role(Role.ADMIN)
                .isEnabled(true)
                .build();
        return userRepository.save(admin);
    }

    private User createManager1() {
        User manager = User.builder()
                .username("duong")
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .fullName("Trần Trí Dương")
                .email("duongtr@gmail.com")
                .phoneNumber("0912345678")
                .role(Role.MANAGER)
                .isEnabled(true)
                .build();
        return userRepository.save(manager);
    }

    private User createManager2() {
        User manager = User.builder()
                .username("quang")
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .fullName("Nguyễn Đăng Quang")
                .email("quangnd@gmail.com")
                .phoneNumber("0923456789")
                .role(Role.MANAGER)
                .isEnabled(true)
                .build();
        return userRepository.save(manager);
    }

    private User createCustomer() {
        User customer = User.builder()
                .username("an")
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .fullName("Đinh Trọng An")
                .email("andt@gmail.com")
                .phoneNumber("0934567890")
                .role(Role.CUSTOMER)
                .isEnabled(true)
                .build();
        return userRepository.save(customer);
    }

    // =====================================================================
    // BADMINTON CLUSTERS
    // =====================================================================

    private BadmintonCluster createCluster1(User manager) {
        BadmintonCluster cluster = BadmintonCluster.builder()
                .name("Vina Badminton - TC5")
                .address("Đừng số 1 Tổng Cục 5, Tân Triều, Thanh Trì, HN")
                .hotLine("0942552468")
                .manager(manager)
                .build();
        return clusterRepository.save(cluster);
    }

    private BadmintonCluster createCluster2(User manager) {
        BadmintonCluster cluster = BadmintonCluster.builder()
                .name("Sân cầu lông TDT")
                .address("315 P.Bùi Xương Trạch, Khương Đình, Thanh Xuân, HN")
                .hotLine("0989787161")
                .manager(manager)
                .build();
        return clusterRepository.save(cluster);
    }

    // =====================================================================
    // COURTS - Cụm sân 1 (Quận 1): 3 sân
    // =====================================================================

    private void createCourtsForCluster1(BadmintonCluster cluster) {

        // Sân A1 - Tiêu chuẩn, đang hoạt động
        courtRepository.save(Court.builder()
                .courtName("Sân A1")
                .type("Standard")
                .imageUrl("https://res.cloudinary.com/demo/image/upload/court_a1.jpg")
                .isAvailable(true)
                .cluster(cluster)
                .build());

        // Sân A2 - VIP, đang hoạt động
        courtRepository.save(Court.builder()
                .courtName("Sân A2")
                .type("VIP")
                .imageUrl("https://res.cloudinary.com/demo/image/upload/court_a2.jpg")
                .isAvailable(true)
                .cluster(cluster)
                .build());

        // Sân A3 - Tiêu chuẩn, đang bảo trì (isAvailable = false)
        courtRepository.save(Court.builder()
                .courtName("Sân A3")
                .type("Standard")
                .imageUrl("https://res.cloudinary.com/demo/image/upload/court_a3.jpg")
                .isAvailable(false)   // Đang bảo trì → dùng để test case này
                .cluster(cluster)
                .build());
    }

    // =====================================================================
    // COURTS - Cụm sân 2 (Quận 7): 3 sân
    // =====================================================================

    private void createCourtsForCluster2(BadmintonCluster cluster) {

        // Sân B1 - Tiêu chuẩn
        courtRepository.save(Court.builder()
                .courtName("Sân B1")
                .type("Standard")
                .imageUrl("https://res.cloudinary.com/demo/image/upload/court_b1.jpg")
                .isAvailable(true)
                .cluster(cluster)
                .build());

        // Sân B2 - Ngoài trời
        courtRepository.save(Court.builder()
                .courtName("Sân B2")
                .type("Outdoor")
                .imageUrl("https://res.cloudinary.com/demo/image/upload/court_b2.jpg")
                .isAvailable(true)
                .cluster(cluster)
                .build());

        // Sân B3 - VIP
        courtRepository.save(Court.builder()
                .courtName("Sân B3")
                .type("VIP")
                .imageUrl("https://res.cloudinary.com/demo/image/upload/court_b3.jpg")
                .isAvailable(true)
                .cluster(cluster)
                .build());
    }
}