package org.example.service;

import org.example.entity.VideoContent;
import org.example.repository.VideoContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VideoContentService {

    @Autowired
    private VideoContentRepository videoContentRepository;

    public VideoContent save(VideoContent content) {
        return videoContentRepository.save(content);
    }

    public List<VideoContent> getContentsByVideoId(Long videoId) {
        List<VideoContent> contents = videoContentRepository.findByVideoIdOrderBySortOrder(videoId);
        System.out.println("=== VideoContentService.getContentsByVideoId ===");
        System.out.println("视频ID: " + videoId);
        System.out.println("查询到的内容数量: " + contents.size());
        for (int i = 0; i < contents.size(); i++) {
            VideoContent content = contents.get(i);
            System.out.println("内容 " + i + ": ID=" + content.getId() +
                ", 类型=" + content.getType() +
                ", 数据=" + content.getData() +
                ", URL=" + content.getUrl());
        }
        return contents;
    }

    @Transactional
    public void deleteByVideoId(Long videoId) {
        videoContentRepository.deleteByVideoId(videoId);
    }
}