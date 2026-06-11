package com.example.badmintonbooking.controller.manager;

import com.example.badmintonbooking.dto.response.ApiResponse;
import com.example.badmintonbooking.dto.response.FileUploadResponse;
import com.example.badmintonbooking.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/manager/courts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ManagerFileController {

    private final IFileStorageService fileStorageService;

    @PostMapping(
            value = "/{courtId}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadCourtImage(
            @PathVariable Long courtId,
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse result = fileStorageService.uploadCourtImage(courtId, file);

        return ResponseEntity.ok(
                ApiResponse.success("Image uploaded successfully", result)
        );
    }
}
