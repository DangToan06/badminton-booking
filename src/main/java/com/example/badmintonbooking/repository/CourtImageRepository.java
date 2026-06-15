package com.example.badmintonbooking.repository;

import com.example.badmintonbooking.entity.CourtImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtImageRepository extends JpaRepository<CourtImage, Long> {
}
