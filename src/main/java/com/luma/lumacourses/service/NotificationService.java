package com.luma.lumacourses.service;

import com.luma.lumacourses.dto.notification.NotificationCreateRequest;
import com.luma.lumacourses.dto.notification.NotificationResponse;
import com.luma.lumacourses.security.principal.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Page<NotificationResponse> listMyNotifications(UserPrincipal principal, Pageable pageable);

    NotificationResponse markAsRead(Long notificationId, UserPrincipal principal);

    NotificationResponse createNotification(NotificationCreateRequest request);

    void deleteNotification(Long notificationId);
}
