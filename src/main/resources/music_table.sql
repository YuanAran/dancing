CREATE TABLE IF NOT EXISTS musics (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    uploader_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_music_uploader_id (uploader_id),
    INDEX idx_music_created_at (created_at),
    CONSTRAINT fk_musics_uploader FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE
);
