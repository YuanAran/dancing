package com.dancing.config;

import com.dancing.until.JwtUntil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器 - 验证Token
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求（CORS预检）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取Token
        String token = request.getHeader("Authorization");
        
        if (token == null || token.isEmpty()) {
            // 如果没有Token，检查是否是公开接口
            String path = request.getRequestURI();
            if (isPublicPath(path)) {
                return true;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
            return false;
        }

        // 解析Token
        Claims claims = JwtUntil.parseToken(token);
        if (claims == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }

        // 将用户名存入request，供Controller使用
        String username = claims.getSubject();
        request.setAttribute("username", username);
        
        return true;
    }

    /**
     * 判断是否是公开接口（不需要认证）
     */
    private boolean isPublicPath(String path) {
        // 注册和登录接口不需要Token
        if (path.startsWith("/api/user/register") || 
            path.startsWith("/api/user/login") ||
            path.equals("/api/user/current")) {
            return true;
        }
        
        // 公开的GET接口（列表、详情、搜索）不需要Token，但如果有Token会获取用户信息
        if (path.startsWith("/api/videos") && 
            (path.equals("/api/videos") || path.matches("/api/videos/\\d+") || path.startsWith("/api/videos/search"))) {
            return true;
        }
        
        if (path.startsWith("/api/posts") && 
            (path.equals("/api/posts/list") || path.matches("/api/posts/\\d+") || path.startsWith("/api/posts/search"))) {
            return true;
        }
        
        // 文件访问接口
        if (path.startsWith("/api/files/")) {
            return true;
        }
        
        return false;
    }
}

