package com.example.marketplace.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        HttpServletRequest request) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "文件为空"));
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 返回绝对URL，确保前端可直接访问
            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            String base = scheme + "://" + host + ":" + port;
            String url = base + "/uploads/" + filename;
            return ResponseEntity.ok(Map.of("url", url));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "上传失败: " + e.getMessage()));
        }
    }
}

