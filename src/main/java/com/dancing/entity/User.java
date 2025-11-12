package com.dancing.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;

    public User() {}

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }



}
