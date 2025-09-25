package org.example.repository;

import org.example.entity.VideoContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoContentRepository extends JpaRepository<VideoContent, Long> {
    List<VideoContent> findByVideoIdOrderBySortOrder(Long videoId);
    void deleteByVideoId(Long videoId);
}