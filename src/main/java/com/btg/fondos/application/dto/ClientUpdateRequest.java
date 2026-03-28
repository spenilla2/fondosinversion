package com.btg.fondos.application.dto;

public record ClientUpdateRequest(
        String name,
        String phone,
        String preferredNotification
) {}
