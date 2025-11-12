package com.dancing.service;

import com.dancing.entity.Post;
import com.dancing.entity.User;
import com.dancing.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 帖子服务类
 */
@Service
public class PostService {

    @Autowired
    private PostMapper postMapper;

    /**
     * 发布帖子
     */
    public boolean createPost(String title, String content, Integer userId) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        Post post = new Post(title, content, userId);
        return postMapper.insert(post) > 0;
    }

    /**
     * 获取所有帖子
     */
    public List<Post> getAllPosts(Integer currentUserId) {
        List<Post> posts = postMapper.findAll();
        
        // 设置当前用户是否已点赞
        if (currentUserId != null) {
            for (Post post : posts) {
                boolean isLiked = postMapper.isLikedByUser(post.getId(), currentUserId);
                post.setIsLiked(isLiked);
            }
        }
        
        return posts;
    }

    /**
     * 根据ID获取帖子
     */
    public Post getPostById(Integer id, Integer currentUserId) {
        Post post = postMapper.findById(id);
        
        // 设置当前用户是否已点赞
        if (post != null && currentUserId != null) {
            boolean isLiked = postMapper.isLikedByUser(id, currentUserId);
            post.setIsLiked(isLiked);
        }
        
        return post;
    }

    /**
     * 获取用户的所有帖子
     */
    public List<Post> getUserPosts(Integer userId, Integer currentUserId) {
        List<Post> posts = postMapper.findByUserId(userId);
        
        // 设置当前用户是否已点赞
        if (currentUserId != null) {
            for (Post post : posts) {
                boolean isLiked = postMapper.isLikedByUser(post.getId(), currentUserId);
                post.setIsLiked(isLiked);
            }
        }
        
        return posts;
    }

    /**
     * 搜索帖子
     */
    public List<Post> searchPosts(String keyword, Integer currentUserId) {
        List<Post> posts = postMapper.searchByKeyword(keyword);
        
        // 设置当前用户是否已点赞
        if (currentUserId != null) {
            for (Post post : posts) {
                boolean isLiked = postMapper.isLikedByUser(post.getId(), currentUserId);
                post.setIsLiked(isLiked);
            }
        }
        
        return posts;
    }

    /**
     * 更新帖子
     */
    public boolean updatePost(Integer postId, String title, String content, Integer userId) {
        Post post = postMapper.findById(postId);
        if (post == null) {
            return false;
        }
        
        // 只有作者可以修改
        if (post.getUserId() == null || !post.getUserId().equals(userId)) {
            return false;
        }

        post.setTitle(title);
        post.setContent(content);
        return postMapper.update(post) > 0;
    }

    /**
     * 删除帖子
     */
    public boolean deletePost(Integer postId, Integer userId) {
        Post post = postMapper.findById(postId);
        if (post == null) {
            return false;
        }
        System.out.println(post.toString());
        // 只有作者可以删除
        if (post.getUserId() == null || !post.getUserId().equals(userId)) {
            return false;
        }

        return postMapper.deleteById(postId) > 0;
    }

    /**
     * 点赞/取消点赞
     */
    @Transactional
    public boolean toggleLike(Integer postId, Integer userId) {
        Post post = postMapper.findById(postId);
        if (post == null) {
            return false;
        }

        boolean isLiked = postMapper.isLikedByUser(postId, userId);
        
        if (isLiked) {
            // 取消点赞
            postMapper.removeLike(postId, userId);
            postMapper.decreaseLikesCount(postId);
        } else {
            // 点赞
            postMapper.addLike(postId, userId);
            postMapper.increaseLikesCount(postId);
        }
        
        return true;
    }

    /**
     * 获取点赞用户列表
     */
    public List<User> getLikeUsers(Integer postId) {
        return postMapper.getLikeUsers(postId);
    }
}

