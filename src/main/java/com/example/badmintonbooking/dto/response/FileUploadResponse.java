package com.example.badmintonbooking.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FileUploadResponse {

    private Long courtId;
    private String courtName;
    private String imageUrl;
}