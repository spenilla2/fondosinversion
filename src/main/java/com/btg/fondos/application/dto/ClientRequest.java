package com.btg.fondos.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank(message = "El usuario es obligatorio") String user,
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotBlank(message = "El email es obligatorio") @Email String email,
        @NotBlank(message = "El teléfono es obligatorio") String phone,
        @NotBlank(message = "La contraseña es obligatoria") String password,
        String preferredNotification
) {}
