package com.taotao.education.user.controller;

import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@Tag(name = "文件管理", description = "文件上传接口")
@RestController
@RequestMapping("/api/user/file")
public class FileController {

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    @Value("${file.access-url:http://localhost:8081/uploads}")
    private String accessUrl;

    private Path uploadDir;

    @PostConstruct
    public void init() {
        // 使用项目根目录下的uploads文件夹
        uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath).toAbsolutePath();
        log.info("文件上传目录: {}", uploadDir);
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }

        // 检查文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException("图片大小不能超过2MB");
        }

        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            // 创建头像目录
            Path avatarDir = uploadDir.resolve("avatar");
            if (!Files.exists(avatarDir)) {
                Files.createDirectories(avatarDir);
                log.info("创建头像目录: {}", avatarDir);
            }

            // 保存文件
            Path destPath = avatarDir.resolve(filename);
            Files.copy(file.getInputStream(), destPath);
            log.info("文件保存成功: {}", destPath);

            // 返回访问URL
            String url = accessUrl + "/avatar/" + filename;
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
}

