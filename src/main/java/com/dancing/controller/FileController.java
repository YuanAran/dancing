package com.dancing.controller;

import com.dancing.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

/**
 * 文件访问控制器 - 提供视频文件流
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private VideoService videoService;

    /**
     * 获取视频文件流
     * @param path 视频文件的相对路径（如：videos/2025/10/1_1234567890.mp4）
     */
    @GetMapping("/video")
    public ResponseEntity<Resource> getVideoFile(@RequestParam String path) {
        try {
            // 将路径中的正斜杠转换为系统分隔符（处理前端传来的路径）
            String normalizedPath = path.replace("/", File.separator);
            // 获取视频文件的完整路径
            String fullPath = videoService.getVideoFullPath(normalizedPath);
            File videoFile = new File(fullPath);

            if (!videoFile.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(videoFile);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("video/mp4"));
            headers.setContentLength(videoFile.length());
            
            // 支持视频流式播放（Range请求）
            headers.set("Accept-Ranges", "bytes");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

