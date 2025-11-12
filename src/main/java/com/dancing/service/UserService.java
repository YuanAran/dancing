package com.dancing.service;

import com.dancing.entity.User;
import com.dancing.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户业务逻辑层
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     */
    public boolean register(User user) {
        try {
            // 检查用户名是否已存在
            User existingUser = userMapper.findByUsername(user.getUsername());
            if (existingUser != null) {
                return false; // 用户名已存在
            }
            
            // 插入新用户
            int result = userMapper.insert(user);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Integer id) {
        return userMapper.findById(id);
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    /**
     * 更新用户信息
     */
    public boolean updateUser(User user) {
        try {
            int result = userMapper.update(user);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(Integer id) {
        try {
            int result = userMapper.deleteById(id);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
