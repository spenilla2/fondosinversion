package com.btg.fondos.infrastructure.adapter.in.rest;

import com.btg.fondos.application.dto.FundRequest;
import com.btg.fondos.application.dto.TransactionResponse;
import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.domain.model.Transaction;
import com.btg.fondos.domain.port.in.FundUseCase;
import com.btg.fondos.domain.port.out.FundRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funds")
@Tag(name = "Fondos", description = "Gestión de fondos de inversión")
@SecurityRequirement(name = "bearerAuth")
public class FundController {

    private final FundUseCase fundUseCase;
    private final FundRepository fundRepository;

    public FundController(FundUseCase fundUseCase, FundRepository fundRepository) {
        this.fundUseCase = fundUseCase;
        this.fundRepository = fundRepository;
    }

    @GetMapping
    @Operation(summary = "Listar todos los fondos disponibles")
    public ResponseEntity<List<Fund>> getAllFunds() {
        return ResponseEntity.ok(fundRepository.findAll());
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @Operation(summary = "Suscribirse a un fondo")
    public ResponseEntity<TransactionResponse> subscribe(Authentication auth,
                                                          @Valid @RequestBody FundRequest request) {
        String clientId = auth.getName();
        Transaction transaction = fundUseCase.subscribe(clientId, request.fundId());
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @Operation(summary = "Cancelar suscripción a un fondo")
    public ResponseEntity<TransactionResponse> cancel(Authentication auth,
                                                       @Valid @RequestBody FundRequest request) {
        String clientId = auth.getName();
        Transaction transaction = fundUseCase.cancel(clientId, request.fundId());
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @Operation(summary = "Ver historial de transacciones")
    public ResponseEntity<List<TransactionResponse>> getTransactions(Authentication auth) {
        String clientId = auth.getName();
        return ResponseEntity.ok(
                fundUseCase.getTransactionHistory(clientId).stream()
                        .map(TransactionResponse::from)
                        .toList()
        );
    }
}
