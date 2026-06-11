package com.example.badmintonbooking.repository;

import com.example.badmintonbooking.entity.User;
import com.example.badmintonbooking.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
        SELECT u FROM User u
        WHERE (:keyword IS NULL OR
               LOWER(u.fullName)  LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(u.username)  LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(u.email)     LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:role IS NULL OR u.role = :role)
        """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            Pageable pageable
    );


    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isEnabled = :isEnabled WHERE u.id = :id")
    void updateEnabledStatus(@Param("id") Long id, @Param("isEnabled") Boolean isEnabled);
}
