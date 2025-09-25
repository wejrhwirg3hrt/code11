package org.example.repository;

import org.example.entity.PrivateAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivateAlbumRepository extends JpaRepository<PrivateAlbum, Long> {
    
    // 根据用户ID查找相册
    List<PrivateAlbum> findByUserIdOrderByCreatedTimeDesc(Long userId);
    
    // 根据用户ID和相册ID查找
    Optional<PrivateAlbum> findByIdAndUserId(Long id, Long userId);
    
    // 根据用户ID和相册名查找
    Optional<PrivateAlbum> findByUserIdAndAlbumName(Long userId, String albumName);
    
    // 统计用户的相册数量
    long countByUserId(Long userId);
}
