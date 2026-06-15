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

import java.util.List;

@RestController
@RequestMapping("/api/v1/manager/courts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ManagerFileController {

    private final IFileStorageService fileStorageService;

    // Trả về 415 nếu k đúng định dạng khai báo trong consumer
    @PostMapping(
            value = "/{courtId}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> uploadCourtImage(
            @PathVariable Long courtId,
            @RequestParam("files") List<MultipartFile> files) {

        List<FileUploadResponse> result = fileStorageService.uploadCourtImages(courtId, files);

        return ResponseEntity.ok(
                ApiResponse.success("Image uploaded successfully", result)
        );
    }
}
