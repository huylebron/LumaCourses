package com.luma.lumacourses.dto.notification;

import com.luma.lumacourses.util.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationCreateRequest(

        @NotNull(message = "User id is required")
        Long userId,

        @NotBlank(message = "Message is required")
        String message,

        NotificationType type,

        @Size(max = 500, message = "Target URL 500 characters")
        String targetUrl) {
}
