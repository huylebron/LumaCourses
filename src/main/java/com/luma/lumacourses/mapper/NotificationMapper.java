package com.luma.lumacourses.mapper;

import com.luma.lumacourses.dto.notification.NotificationResponse;
import com.luma.lumacourses.entity.Notification;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                notification.getMessage(),
                notification.getType(),
                notification.getTargetUrl(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}
