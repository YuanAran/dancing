package com.dancing.mapper;

import com.dancing.entity.Music;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MusicMapper {

    @Insert("INSERT INTO musics(title, description, file_path, uploader_id) VALUES(#{title}, #{description}, #{filePath}, #{uploaderId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Music music);

    @Select("SELECT m.*, u.username as uploaderName FROM musics m " +
            "LEFT JOIN users u ON m.uploader_id = u.id " +
            "ORDER BY m.created_at DESC")
    List<Music> findAll();

    @Select("SELECT m.*, u.username as uploaderName FROM musics m " +
            "LEFT JOIN users u ON m.uploader_id = u.id " +
            "WHERE m.id = #{id}")
    Music findById(Integer id);

    @Select("SELECT m.*, u.username as uploaderName FROM musics m " +
            "LEFT JOIN users u ON m.uploader_id = u.id " +
            "WHERE m.uploader_id = #{uploaderId} " +
            "ORDER BY m.created_at DESC")
    List<Music> findByUploaderId(Integer uploaderId);

    @Select("SELECT m.*, u.username as uploaderName FROM musics m " +
            "LEFT JOIN users u ON m.uploader_id = u.id " +
            "WHERE m.title LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY m.created_at DESC")
    List<Music> searchByTitle(String keyword);

    @Delete("DELETE FROM musics WHERE id = #{id}")
    int deleteById(Integer id);
}
