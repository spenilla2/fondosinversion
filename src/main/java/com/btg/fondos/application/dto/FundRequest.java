package com.btg.fondos.application.dto;

import jakarta.validation.constraints.NotBlank;

public record FundRequest(
        @NotBlank(message = "El ID del fondo es obligatorio")
        String fundId
) {}
