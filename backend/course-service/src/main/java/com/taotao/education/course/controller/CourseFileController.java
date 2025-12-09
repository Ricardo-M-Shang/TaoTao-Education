package com.taotao.education.course.controller;

import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 课程文件上传（视频/图文资源）
 */
@Slf4j
@Tag(name = "课程文件", description = "课程文件上传接口")
@RestController
@RequestMapping("/api/course/file")
public class CourseFileController {

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    @Value("${file.access-url:http://localhost:8082/uploads}")
    private String accessUrl;

    private Path uploadDir;

    @PostConstruct
    public void init() {
        uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath).toAbsolutePath();
        log.info("课程文件上传目录: {}", uploadDir);
    }

    @Operation(summary = "上传课程视频")
    @PostMapping("/video")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的视频文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new BusinessException("只能上传视频文件");
        }

        // 限制大小：500MB
        if (file.getSize() > 500L * 1024 * 1024) {
            throw new BusinessException("视频大小不能超过500MB");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".mp4";
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            Path videoDir = uploadDir.resolve("video");
            if (!Files.exists(videoDir)) {
                Files.createDirectories(videoDir);
                log.info("创建视频目录: {}", videoDir);
            }

            Path destPath = videoDir.resolve(filename);
            Files.copy(file.getInputStream(), destPath);
            log.info("视频保存成功: {}", destPath);

            String url = accessUrl + "/video/" + filename;
            return Result.success(url);
        } catch (IOException e) {
            log.error("视频上传失败", e);
            throw new BusinessException("视频上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传课程封面")
    @PostMapping("/cover")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }

        // 限制大小：5MB
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new BusinessException("图片大小不能超过5MB");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            Path imgDir = uploadDir.resolve("cover");
            if (!Files.exists(imgDir)) {
                Files.createDirectories(imgDir);
                log.info("创建封面目录: {}", imgDir);
            }

            Path destPath = imgDir.resolve(filename);
            Files.copy(file.getInputStream(), destPath);
            log.info("封面保存成功: {}", destPath);

            String url = accessUrl + "/cover/" + filename;
            return Result.success(url);
        } catch (IOException e) {
            log.error("封面上传失败", e);
            throw new BusinessException("封面上传失败: " + e.getMessage());
        }
    }
}

