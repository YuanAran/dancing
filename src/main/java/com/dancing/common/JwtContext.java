package com.dancing.common;

import com.dancing.entity.User;
import com.dancing.service.UserService;
import com.dancing.until.JwtUntil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * JWT上下文工具类 - 从请求中获取当前用户
 */
@Component
public class JwtContext {

    @Autowired
    private UserService userService;

    /**
     * 从请求头中获取当前用户
     */
    public User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            String username = JwtUntil.getUsername(token);
            if (username != null) {
                return userService.getUserByUsername(username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 从请求头中获取当前用户ID
     */
    public Integer getCurrentUserId(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null ? user.getId() : null;
    }

    /**
     * 从请求头中获取当前用户名
     */
    public String getCurrentUsername(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            return JwtUntil.getUsername(token);
        } catch (Exception e) {
            return null;
        }
    }
}

