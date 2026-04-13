package com.luma.lumacourses.repository;

import com.luma.lumacourses.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
