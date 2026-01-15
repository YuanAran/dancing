package com.dancing.service;

import com.dancing.entity.Comment;
import com.dancing.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> getPostComments(Integer postId) {
        return commentMapper.findByPostId(postId);
    }

    public boolean createPostComment(Integer postId, Integer userId, String content) {
        if (postId == null || userId == null) {
            return false;
        }
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        Comment comment = new Comment(content, userId, null, postId);
        return commentMapper.insert(comment) > 0;
    }

    public boolean deleteComment(Integer commentId, Integer userId) {
        if (commentId == null || userId == null) {
            return false;
        }

        Comment comment = commentMapper.findById(commentId);
        if (comment == null) {
            return false;
        }
        if (comment.getUserId() == null || !comment.getUserId().equals(userId)) {
            return false;
        }

        return commentMapper.deleteById(commentId) > 0;
    }
}
