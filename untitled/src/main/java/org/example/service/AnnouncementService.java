package org.example.service;

import org.example.entity.Announcement;
import org.example.entity.User;
import org.example.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = false)
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    public List<Announcement> getActiveAnnouncements() {
        return announcementRepository.findByActiveOrderByCreatedAtDesc(true);
    }


    public Announcement createAnnouncement(String title, String content, User createdBy) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setCreatedBy(createdBy);
        return announcementRepository.save(announcement);
    }

    public void toggleAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id).orElse(null);
        if (announcement != null) {
            announcement.setActive(!announcement.isActive());
            announcementRepository.save(announcement);
        }
    }

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }
}


