package com.dancing.mapper;

import com.dancing.entity.Video;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 视频数据访问接口
 */
@Mapper
public interface VideoMapper {

    /**
     * 插入新视频
     */
    @Insert("INSERT INTO videos(title, description, file_path, uploader_id) VALUES(#{title}, #{description}, #{filePath}, #{uploaderId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Video video);

    /**
     * 根据ID查找视频（关联用户表获取上传者名称）
     */
    @Select("SELECT v.*, u.username as uploaderName FROM videos v " +
            "LEFT JOIN users u ON v.uploader_id = u.id " +
            "WHERE v.id = #{id}")
    Video findById(Integer id);

    /**
     * 查找所有视频（关联用户表获取上传者名称）
     */
    @Select("SELECT v.*, u.username as uploaderName FROM videos v " +
            "LEFT JOIN users u ON v.uploader_id = u.id " +
            "ORDER BY v.created_at DESC")
    List<Video> findAll();

    /**
     * 根据上传者ID查找视频（关联用户表获取上传者名称）
     */
    @Select("SELECT v.*, u.username as uploaderName FROM videos v " +
            "LEFT JOIN users u ON v.uploader_id = u.id " +
            "WHERE v.uploader_id = #{uploaderId} " +
            "ORDER BY v.created_at DESC")
    List<Video> findByUploaderId(Integer uploaderId);

    /**
     * 根据标题搜索视频（关联用户表获取上传者名称）
     */
    @Select("SELECT v.*, u.username as uploaderName FROM videos v " +
            "LEFT JOIN users u ON v.uploader_id = u.id " +
            "WHERE v.title LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY v.created_at DESC")
    List<Video> searchByTitle(String keyword);

    /**
     * 删除视频
     */
    @Delete("DELETE FROM videos WHERE id = #{id}")
    int deleteById(Integer id);
}
