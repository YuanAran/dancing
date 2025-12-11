package com.dancing.service;

import com.dancing.entity.Video;
import com.dancing.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * 视频业务逻辑层
 */
@Service
public class VideoService {

    @Autowired
    private VideoMapper videoMapper;

    @Value("${file.upload.path}")
    private String uploadBasePath;

    @Value("${video.path.prefix}")
    private String videoPathPrefix;

    /**
     * 上传视频
     */
    public boolean uploadVideo(MultipartFile file, String title, String description, Integer uploaderId) {
        try {
            // 1. 检查文件类型
            if (!isValidVideoFile(file)) {
                return false;
            }

            // 2. 生成文件路径
            String relativePath = generateVideoPath(file, uploaderId);

            // 3. 创建目录
            String fullPath = uploadBasePath + relativePath;
            File targetFile = new File(fullPath);
            File parentDir = targetFile.getParentFile();
            
            if (!parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created && !parentDir.exists()) {
                    return false;
                }
            }

            // 4. 保存文件
            file.transferTo(targetFile);

            String thumbnailRelativePath = generateThumbnailPath(relativePath);
            File thumbnailFile = new File(uploadBasePath + thumbnailRelativePath);
            File thumbnailParent = thumbnailFile.getParentFile();
            if (!thumbnailParent.exists()) {
                thumbnailParent.mkdirs();
            }
            generateVideoThumbnail(targetFile, thumbnailFile);

            // 5. 保存视频信息到数据库
            Video video = new Video(title, description, relativePath, uploaderId);
            video.setThumbnailPath(thumbnailRelativePath);
            int result = videoMapper.insert(video);
            
            return result > 0;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取所有视频
     */
    public List<Video> getAllVideos() {
        return videoMapper.findAll();
    }

    /**
     * 根据ID获取视频
     */
    public Video getVideoById(Integer id) {
        return videoMapper.findById(id);
    }

    /**
     * 根据上传者ID获取视频
     */
    public List<Video> getVideosByUploaderId(Integer uploaderId) {
        return videoMapper.findByUploaderId(uploaderId);
    }

    /**
     * 搜索视频
     */
    public List<Video> searchVideos(String keyword) {
        return videoMapper.searchByTitle(keyword);
    }

    /**
     * 删除视频
     */
    public boolean deleteVideo(Integer id) {
        try {
            // 1. 获取视频信息
            Video video = videoMapper.findById(id);
            if (video == null) {
                return false;
            }

            // 2. 删除文件
            String fullPath = uploadBasePath + video.getFilePath();
            File file = new File(fullPath);
            if (file.exists()) {
                file.delete();
            }

            if (video.getThumbnailPath() != null) {
                File thumbFile = new File(uploadBasePath + video.getThumbnailPath());
                if (thumbFile.exists()) {
                    thumbFile.delete();
                }
            }

            // 3. 删除数据库记录
            int result = videoMapper.deleteById(id);
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查是否为有效的视频文件
     */
    private boolean isValidVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        return contentType.startsWith("video/") || 
               file.getOriginalFilename().toLowerCase().endsWith(".mp4") ||
               file.getOriginalFilename().toLowerCase().endsWith(".avi") ||
               file.getOriginalFilename().toLowerCase().endsWith(".mov") ||
               file.getOriginalFilename().toLowerCase().endsWith(".wmv");
    }

    /**
     * 生成视频文件路径
     */
    private String generateVideoPath(MultipartFile file, Integer uploaderId) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String filename = uploaderId + "_" + System.currentTimeMillis() + extension;
        
        // 使用File.separator确保跨平台兼容性
        return videoPathPrefix + year + File.separator + month + File.separator + filename;
    }

    private String generateThumbnailPath(String videoRelativePath) {
        int idx = videoRelativePath.lastIndexOf('.')
;        String base = idx > -1 ? videoRelativePath.substring(0, idx) : videoRelativePath;
        return base + "_thumb.jpg";
    }

    private void generateVideoThumbnail(File videoFile, File thumbnailFile) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", videoFile.getAbsolutePath(),
                    "-ss", "00:00:01",
                    "-vframes", "1",
                    thumbnailFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取视频文件的完整路径
     */
    public String getVideoFullPath(String relativePath) {
        // 确保路径正确拼接
        String basePath = uploadBasePath;
        if (!basePath.endsWith(File.separator) && !basePath.endsWith("/") && !basePath.endsWith("\\")) {
            basePath += File.separator;
        }
        // 移除相对路径开头的斜杠
        String cleanPath = relativePath.replaceFirst("^[/\\\\]+", "");
        return basePath + cleanPath;
    }

    /**
     * 检查视频文件是否存在
     */
    public boolean videoFileExists(String relativePath) {
        File file = new File(getVideoFullPath(relativePath));
        return file.exists();
    }
}
