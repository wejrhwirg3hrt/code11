package org.example.repository;

import org.example.entity.Violation;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ViolationRepository extends JpaRepository<Violation, Long> {
    List<Violation> findByUserOrderByCreatedAtDesc(User user);
    List<Violation> findAllByOrderByCreatedAtDesc();
}