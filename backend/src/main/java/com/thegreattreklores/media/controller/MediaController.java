package com.thegreattreklores.media.controller;

import com.thegreattreklores.common.api.ApiResponse;
import com.thegreattreklores.media.dto.MediaDtos.UploadResponse;
import com.thegreattreklores.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Tag(name = "Media Storage", description = "Endpoints for uploading image files to Cloudinary/S3 object storage")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image", description = "Uploads an image (JPEG, PNG, WebP up to 5MB) and returns a storage URL")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(@RequestParam("file") MultipartFile file) {
        UploadResponse response = mediaService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
    }
}
