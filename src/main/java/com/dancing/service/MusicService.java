package com.dancing.service;

import com.dancing.entity.Music;
import com.dancing.mapper.MusicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class MusicService {

    @Autowired
    private MusicMapper musicMapper;

    @Value("${file.upload.path}")
    private String uploadBasePath;

    @Value("${music.path.prefix:musics/}")
    private String musicPathPrefix;

    public boolean uploadMusic(MultipartFile file, String title, String description, Integer uploaderId) {
        try {
            if (!isValidMusicFile(file)) {
                return false;
            }

            String relativePath = generateMusicPath(file, uploaderId);
            String fullPath = uploadBasePath + relativePath;
            File targetFile = new File(fullPath);
            File parentDir = targetFile.getParentFile();

            if (!parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created && !parentDir.exists()) {
                    return false;
                }
            }

            file.transferTo(targetFile);

            Music music = new Music(title, description, relativePath, uploaderId);
            int result = musicMapper.insert(music);
            return result > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Music> getAllMusics() {
        return musicMapper.findAll();
    }

    public Music getMusicById(Integer id) {
        return musicMapper.findById(id);
    }

    public List<Music> getMusicsByUploaderId(Integer uploaderId) {
        return musicMapper.findByUploaderId(uploaderId);
    }

    public List<Music> searchMusics(String keyword) {
        return musicMapper.searchByTitle(keyword);
    }

    public boolean deleteMusic(Integer id) {
        try {
            Music music = musicMapper.findById(id);
            if (music == null) {
                return false;
            }

            File file = new File(getMusicFullPath(music.getFilePath()));
            if (file.exists()) {
                file.delete();
            }

            return musicMapper.deleteById(id) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getMusicFullPath(String relativePath) {
        String basePath = uploadBasePath;
        if (!basePath.endsWith(File.separator) && !basePath.endsWith("/") && !basePath.endsWith("\\")) {
            basePath += File.separator;
        }
        String cleanPath = relativePath.replaceFirst("^[/\\\\]+", "");
        return basePath + cleanPath;
    }

    private boolean isValidMusicFile(MultipartFile file) {
        String contentType = file.getContentType();
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        return (contentType != null && contentType.startsWith("audio/"))
                || name.endsWith(".mp3")
                || name.endsWith(".wav")
                || name.endsWith(".ogg")
                || name.endsWith(".m4a")
                || name.endsWith(".flac");
    }

    private String generateMusicPath(MultipartFile file, Integer uploaderId) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = uploaderId + "_" + System.currentTimeMillis() + extension;
        return musicPathPrefix + year + File.separator + month + File.separator + filename;
    }
}
