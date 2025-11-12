package com.dancing.controller;

import com.dancing.common.ApiResponse;
import com.dancing.common.JwtContext;
import com.dancing.entity.User;
import com.dancing.entity.Video;
import com.dancing.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 视频控制器 - 为Vue3前端提供视频相关RESTful接口
 */
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private JwtContext jwtContext;

    /**
     * 获取视频列表
     */
    @GetMapping
    public ApiResponse<List<Video>> getVideoList() {
        try {
            List<Video> videos = videoService.getAllVideos();
            return ApiResponse.success(videos);
        } catch (Exception e) {
            return ApiResponse.error("获取视频列表失败：" + e.getMessage());
        }
    }

    /**
     * 上传视频
     */
    @PostMapping("/upload")
    public ApiResponse<String> uploadVideo(@RequestParam("file") MultipartFile file,
                                          @RequestParam("title") String title,
                                          @RequestParam("description") String description,
                                          HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            if (file.isEmpty()) {
                return ApiResponse.badRequest("请选择要上传的视频文件");
            }

            boolean success = videoService.uploadVideo(file, title, description, user.getId());
            
            if (success) {
                return ApiResponse.success("视频上传成功");
            } else {
                return ApiResponse.error("视频上传失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("视频上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取视频详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Video> getVideoDetail(@PathVariable Integer id) {
        try {
            Video video = videoService.getVideoById(id);
            if (video != null) {
                return ApiResponse.success(video);
            } else {
                return ApiResponse.notFound("视频不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取视频详情失败：" + e.getMessage());
        }
    }

    /**
     * 获取我的视频
     */
    @GetMapping("/my")
    public ApiResponse<List<Video>> getMyVideos(HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            List<Video> videos = videoService.getVideosByUploaderId(user.getId());
            return ApiResponse.success(videos);
        } catch (Exception e) {
            return ApiResponse.error("获取我的视频失败：" + e.getMessage());
        }
    }

    /**
     * 删除视频
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteVideo(@PathVariable Integer id, HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            Video video = videoService.getVideoById(id);
            if (video == null) {
                return ApiResponse.notFound("视频不存在");
            }

            // 检查权限：只有上传者可以删除
            if (!video.getUploaderId().equals(user.getId())) {
                return ApiResponse.forbidden("无权限删除此视频");
            }

            boolean success = videoService.deleteVideo(id);
            if (success) {
                return ApiResponse.success("视频删除成功");
            } else {
                return ApiResponse.error("视频删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("视频删除失败：" + e.getMessage());
        }
    }

    /**
     * 搜索视频
     */
    @GetMapping("/search")
    public ApiResponse<List<Video>> searchVideos(@RequestParam String keyword) {
        try {
            List<Video> videos = videoService.searchVideos(keyword);
            return ApiResponse.success(videos);
        } catch (Exception e) {
            return ApiResponse.error("搜索视频失败：" + e.getMessage());
        }
    }
}

