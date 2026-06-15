package com.example.badmintonbooking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "court_images")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CourtImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;
}
