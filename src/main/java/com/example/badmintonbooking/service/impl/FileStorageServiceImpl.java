package com.example.badmintonbooking.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.badmintonbooking.dto.response.FileUploadResponse;
import com.example.badmintonbooking.entity.Court;
import com.example.badmintonbooking.exception.CloudStorageException;
import com.example.badmintonbooking.exception.CustomExceptions;
import com.example.badmintonbooking.repository.CourtRepository;
import com.example.badmintonbooking.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements IFileStorageService {

    private final Cloudinary cloudinary;
    private final CourtRepository courtRepository;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    @Override
    public FileUploadResponse uploadCourtImage(Long courtId, MultipartFile file) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new CustomExceptions.CourtNotFoundException(courtId));

        validateFile(file);

        String imageUrl = uploadToCloudinary(file, courtId);

        courtRepository.updateImageUrl(courtId, imageUrl);
        log.info("Updated image for court '{}' (id: {}) → {}", court.getCourtName(), courtId, imageUrl);

        return FileUploadResponse.builder()
                .courtId(courtId)
                .courtName(court.getCourtName())
                .imageUrl(imageUrl)
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomExceptions.InvalidFileException("File is empty or not provided");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomExceptions.InvalidFileException(
                    "Invalid file type: '" + contentType + "'. Allowed: JPEG, JPG, PNG, WEBP"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomExceptions.InvalidFileException(
                    "File '" + file.getOriginalFilename() + "' exceeds maximum size of 5MB"
            );
        }
    }

    private String uploadToCloudinary(MultipartFile file, Long courtId) {
        try {
            String publicId = String.format("courts/court_%d_%d", courtId, System.currentTimeMillis());

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id",     publicId,
                            "folder",        "badminton_booking",
                            "overwrite",     false,
                            "resource_type", "image"
                    )
            );

            String secureUrl = (String) result.get("secure_url");
            log.info("Cloudinary upload success: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("Cloudinary upload failed for court {}: {}", courtId, e.getMessage());
            throw new CloudStorageException("Failed to upload image to cloud storage", e);
        }
    }
}
