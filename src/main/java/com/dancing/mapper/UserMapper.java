package com.dancing.mapper;

import com.dancing.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper {

    /**
     * 插入新用户
     */
    @Insert("INSERT INTO users(username, password, email) VALUES(#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    /**
     * 根据ID查找用户
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Integer id);

    /**
     * 查找所有用户
     */
    @Select("SELECT * FROM users ORDER BY created_at DESC")
    List<User> findAll();

    /**
     * 更新用户信息
     */
    @Update("UPDATE users SET username = #{username}, email = #{email} WHERE id = #{id}")
    int update(User user);

    /**
     * 删除用户
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Integer id);
}
