package com.example.badmintonbooking.service;

import com.example.badmintonbooking.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFileStorageService {

    List<FileUploadResponse> uploadCourtImages(Long courtId, List<MultipartFile> files);



}
