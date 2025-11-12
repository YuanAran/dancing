package com.dancing.controller;

import com.dancing.common.ApiResponse;
import com.dancing.common.JwtContext;
import com.dancing.entity.User;
import com.dancing.service.UserService;
import com.dancing.until.JwtUntil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器 - 为Vue3前端提供用户相关RESTful接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtContext jwtContext;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            String email = params.get("email");
            
            User user = new User(username, password, email);
            boolean success = userService.register(user);
            if (success) {
                return ApiResponse.success("注册成功");
            } else {
                return ApiResponse.badRequest("注册失败，用户名可能已存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            
            User user = userService.login(username, password);
            if (user != null) {
                // 生成JWT Token
                String token = JwtUntil.generateToken(username);
                
                // 返回用户信息和Token
                Map<String, Object> result = new HashMap<>();
                result.put("user", user);
                result.put("token", token);
                
                return ApiResponse.success("登录成功", result);
            } else {
                return ApiResponse.unauthorized("用户名或密码错误");
            }
        } catch (Exception e) {
            return ApiResponse.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ApiResponse<User> getCurrentUser(HttpServletRequest request) {
        User user = jwtContext.getCurrentUser(request);
        if (user != null) {
            // 重新获取用户信息
            User currentUser = userService.getUserById(user.getId());
            return ApiResponse.success(currentUser);
        } else {
            return ApiResponse.unauthorized("用户未登录");
        }
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        // JWT是无状态的，前端删除Token即可
        return ApiResponse.success("退出成功");
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    public ApiResponse<String> updateUser(@RequestBody Map<String, String> params,
                                         HttpServletRequest request) {
        try {
            User currentUser = jwtContext.getCurrentUser(request);
            if (currentUser == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            String username = params.get("username");
            String email = params.get("email");

            User user = new User();
            user.setId(currentUser.getId());
            user.setUsername(username);
            user.setEmail(email);

            boolean success = userService.updateUser(user);
            if (success) {
                return ApiResponse.success("更新成功");
            } else {
                return ApiResponse.error("更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("更新失败：" + e.getMessage());
        }
    }
}

