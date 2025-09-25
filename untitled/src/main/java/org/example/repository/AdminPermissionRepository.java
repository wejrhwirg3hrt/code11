package org.example.repository;

import org.example.entity.AdminPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Long> {

    Optional<AdminPermission> findByAdminIdAndActiveTrue(Long adminId);

    void deleteByAdminId(Long adminId);
}