package org.example.repository;

import org.example.entity.PrivatePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivatePhotoRepository extends JpaRepository<PrivatePhoto, Long> {
    
    // 根据相册ID查找照片
    List<PrivatePhoto> findByAlbumIdOrderByUploadTimeDesc(Long albumId);
    
    // 统计相册中的照片数量
    long countByAlbumId(Long albumId);
    
    // 删除相册中的所有照片
    void deleteByAlbumId(Long albumId);
}
