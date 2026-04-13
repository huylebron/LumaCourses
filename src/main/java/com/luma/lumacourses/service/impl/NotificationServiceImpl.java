package com.luma.lumacourses.service.impl;

import com.luma.lumacourses.dto.notification.NotificationCreateRequest;
import com.luma.lumacourses.dto.notification.NotificationResponse;
import com.luma.lumacourses.entity.Notification;
import com.luma.lumacourses.entity.User;
import com.luma.lumacourses.mapper.NotificationMapper;
import com.luma.lumacourses.repository.NotificationRepository;
import com.luma.lumacourses.repository.UserRepository;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.NotificationService;
import com.luma.lumacourses.util.enums.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> listMyNotifications(UserPrincipal principal, Pageable pageable) {
        return notificationRepository.findByUserId(principal.getUserId(), pageable)
                .map(NotificationMapper::toResponse);
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId, UserPrincipal principal) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, principal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
            log.info("Notification marked as read: notificationId={}, userId={}", notificationId, principal.getUserId());
        }

        return NotificationMapper.toResponse(notification);
    }

    @Override
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.userId()));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(request.message());
        notification.setType(request.type() != null ? request.type() : NotificationType.GENERIC);
        notification.setTargetUrl(request.targetUrl());
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: notificationId={}, userId={}", saved.getId(), user.getId());
        return NotificationMapper.toResponse(saved);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + notificationId));

        notificationRepository.delete(notification);
        log.info("Notification deleted: notificationId={}", notificationId);
    }
}
