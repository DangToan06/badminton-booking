package com.example.badmintonbooking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "courts")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "court_name", nullable = false, length = 50)
    private String courtName;

    @Column(length = 50)
    private String type;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourtImage> images;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private BadmintonCluster cluster;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;
}