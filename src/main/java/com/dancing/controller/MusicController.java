package com.dancing.controller;

import com.dancing.common.ApiResponse;
import com.dancing.common.JwtContext;
import com.dancing.entity.Music;
import com.dancing.entity.User;
import com.dancing.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    @Autowired
    private MusicService musicService;

    @Autowired
    private JwtContext jwtContext;

    @GetMapping
    public ApiResponse<List<Music>> getMusicList() {
        try {
            return ApiResponse.success(musicService.getAllMusics());
        } catch (Exception e) {
            return ApiResponse.error("获取音乐列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ApiResponse<String> uploadMusic(@RequestParam("file") MultipartFile file,
                                           @RequestParam("title") String title,
                                           @RequestParam("description") String description,
                                           HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }
            if (file.isEmpty()) {
                return ApiResponse.badRequest("请选择要上传的音乐文件");
            }
            boolean success = musicService.uploadMusic(file, title, description, user.getId());
            return success ? ApiResponse.success("音乐上传成功") : ApiResponse.error("音乐上传失败");
        } catch (Exception e) {
            return ApiResponse.error("音乐上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/my")
    public ApiResponse<List<Music>> getMyMusic(HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }
            return ApiResponse.success(musicService.getMusicsByUploaderId(user.getId()));
        } catch (Exception e) {
            return ApiResponse.error("获取我的音乐失败：" + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ApiResponse<List<Music>> searchMusic(@RequestParam String keyword) {
        try {
            return ApiResponse.success(musicService.searchMusics(keyword));
        } catch (Exception e) {
            return ApiResponse.error("搜索音乐失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteMusic(@PathVariable Integer id, HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }
            Music music = musicService.getMusicById(id);
            if (music == null) {
                return ApiResponse.notFound("音乐不存在");
            }
            if (!music.getUploaderId().equals(user.getId())) {
                return ApiResponse.forbidden("无权限删除此音乐");
            }
            return musicService.deleteMusic(id) ? ApiResponse.success("音乐删除成功") : ApiResponse.error("音乐删除失败");
        } catch (Exception e) {
            return ApiResponse.error("音乐删除失败：" + e.getMessage());
        }
    }
}
