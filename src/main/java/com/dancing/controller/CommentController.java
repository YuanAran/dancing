package com.dancing.controller;

import com.dancing.common.ApiResponse;
import com.dancing.common.JwtContext;
import com.dancing.entity.Comment;
import com.dancing.entity.User;
import com.dancing.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private JwtContext jwtContext;

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<Comment>> getPostComments(@PathVariable Integer postId) {
        List<Comment> comments = commentService.getPostComments(postId);
        return ApiResponse.success(comments);
    }

    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<String> createPostComment(@PathVariable Integer postId,
                                                @RequestBody Map<String, String> params,
                                                HttpServletRequest request) {
        User user = jwtContext.getCurrentUser(request);
        if (user == null) {
            return ApiResponse.unauthorized("用户未登录");
        }

        String content = params.get("content");
        boolean success = commentService.createPostComment(postId, user.getId(), content);
        if (success) {
            return ApiResponse.success("评论成功");
        }
        return ApiResponse.badRequest("评论失败");
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<String> deleteComment(@PathVariable Integer commentId, HttpServletRequest request) {
        User user = jwtContext.getCurrentUser(request);
        if (user == null) {
            return ApiResponse.unauthorized("用户未登录");
        }

        boolean success = commentService.deleteComment(commentId, user.getId());
        if (success) {
            return ApiResponse.success("删除成功");
        }
        return ApiResponse.forbidden("删除失败或无权限");
    }
}
