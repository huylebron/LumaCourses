package com.luma.lumacourses.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.luma.lumacourses.util.enums.NotificationType;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationResponse(
        Long id,
        Long userId,
        String message,
        NotificationType type,
        String targetUrl,
        boolean read,
        LocalDateTime createdAt) {
}
