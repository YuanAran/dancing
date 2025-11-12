package com.dancing.controller;

import com.dancing.common.ApiResponse;
import com.dancing.common.JwtContext;
import com.dancing.entity.Post;
import com.dancing.entity.User;
import com.dancing.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 帖子控制器
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private JwtContext jwtContext;

    /**
     * 发布帖子
     */
    @PostMapping("/create")
    public ApiResponse<String> createPost(@RequestBody Map<String, String> params, HttpServletRequest request) {
        User user = jwtContext.getCurrentUser(request);
        if (user == null) {
            return ApiResponse.unauthorized("用户未登录");
        }

        String title = params.get("title");
        String content = params.get("content");

        if (title == null || title.trim().isEmpty()) {
            return ApiResponse.badRequest("帖子标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.badRequest("帖子内容不能为空");
        }

        boolean success = postService.createPost(title, content, user.getId());
        if (success) {
            return ApiResponse.success("发布成功");
        } else {
            return ApiResponse.error("发布失败");
        }
    }

    /**
     * 获取所有帖子
     */
    @GetMapping("/list")
    public ApiResponse<List<Post>> getAllPosts(HttpServletRequest request) {
        User user = jwtContext.getCurrentUser(request);
        Integer currentUserId = user != null ? user.getId() : null;
        
        List<Post> posts = postService.getAllPosts(currentUserId);
        return ApiResponse.success(posts);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Post> getPostDetail(@PathVariable Integer id, HttpServletRequest request) {
        User user = jwtContext.getCurrentUser(request);
        Integer currentUserId = user != null ? user.getId() : null;
        
        Post post = postService.getPostById(id, currentUserId);
        if (post != null) {
            return ApiResponse.success(post);
        } else {
            return ApiResponse.notFound("帖子不存在");
        }
    }

    /**
     * 获取我的帖子
     */
    @GetMapping("/my")
    public ApiResponse<List<Post>> getMyPosts(HttpServletRequest request) {
        User user = jwtContext.getCurrentUser(request);
        if (user == null) {
            return ApiResponse.unauthorized("用户未登录");
        }

        List<Post> posts = postService.getUserPosts(user.getId(), user.getId());
        return ApiResponse.success(posts);
    }

    /**
     * 获取指定用户的帖子
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Post>> getUserPosts(@PathVariable Integer userId, HttpServletRequest request) {
        User user = jwtContext.getCurrentUser(request);
        Integer currentUserId = user != null ? user.getId() : null;
        
        List<Post> posts = postService.getUserPosts(userId, currentUserId);
        return ApiResponse.success(posts);
    }

    /**
     * 搜索帖子
     */
    @GetMapping("/search")
    public ApiResponse<List<Post>> searchPosts(@RequestParam String keyword, HttpServletRequest request) {
        User user = jwtContext.getCurrentUser(request);
        Integer currentUserId = user != null ? user.getId() : null;
        
        List<Post> posts = postService.searchPosts(keyword, currentUserId);
        return ApiResponse.success(posts);
    }

    /**
     * 更新帖子
     */
    @PutMapping("/{id}")
    public ApiResponse<String> updatePost(@PathVariable Integer id,
                                         @RequestBody Map<String, String> params,
                                         HttpServletRequest request) {
        User user = jwtContext.getCurrentUser(request);
        if (user == null) {
            return ApiResponse.unauthorized("用户未登录");
        }

        String title = params.get("title");
        String content = params.get("content");

        boolean success = postService.updatePost(id, title, content, user.getId());
        if (success) {
            return ApiResponse.success("更新成功");
        } else {
            return ApiResponse.error("更新失败或无权限");
        }
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deletePost(@PathVariable Integer id, HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            boolean success = postService.deletePost(id, user.getId());
            System.out.println(user.getId());
            if (success) {
                return ApiResponse.success("删除成功");
            } else {
                return ApiResponse.error("删除失败或无权限");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{id}/like")
    public ApiResponse<String> toggleLike(@PathVariable Integer id, HttpServletRequest request) {
        User user = jwtContext.getCurrentUser(request);
        if (user == null) {
            return ApiResponse.unauthorized("用户未登录");
        }

        boolean success = postService.toggleLike(id, user.getId());
        if (success) {
            return ApiResponse.success("操作成功");
        } else {
            return ApiResponse.error("操作失败");
        }
    }

    /**
     * 获取点赞用户列表
     */
    @GetMapping("/{id}/likes")
    public ApiResponse<List<User>> getLikeUsers(@PathVariable Integer id) {
        List<User> users = postService.getLikeUsers(id);
        return ApiResponse.success(users);
    }
}

