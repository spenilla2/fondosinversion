package com.btg.fondos.infrastructure.adapter.in.rest;

import com.btg.fondos.application.dto.ClientRequest;
import com.btg.fondos.application.dto.ClientResponse;
import com.btg.fondos.application.dto.ClientUpdateRequest;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.port.in.ClientUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clientes", description = "Gestión de clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private final ClientUseCase clientUseCase;

    public ClientController(ClientUseCase clientUseCase) {
        this.clientUseCase = clientUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Crear un nuevo cliente")
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        Client client = clientUseCase.create(
                request.user(), request.name(), request.email(), request.phone(),
                request.password(), request.preferredNotification());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClientResponse.from(client));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Listar todos los clientes")
    public ResponseEntity<List<ClientResponse>> getAll() {
        return ResponseEntity.ok(
                clientUseCase.getAll().stream()
                        .map(ClientResponse::from)
                        .toList()
        );
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Buscar cliente por usuario")
    public ResponseEntity<ClientResponse> getByUser(@RequestParam String user) {
        return ResponseEntity.ok(ClientResponse.from(clientUseCase.getByUser(user)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #id == authentication.name")
    @Operation(summary = "Obtener cliente por ID")
    public ResponseEntity<ClientResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(ClientResponse.from(clientUseCase.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #id == authentication.name")
    @Operation(summary = "Actualizar cliente")
    public ResponseEntity<ClientResponse> update(@PathVariable String id,
                                                  @RequestBody ClientUpdateRequest request) {
        Client client = clientUseCase.update(id, request.name(), request.phone(), request.preferredNotification());
        return ResponseEntity.ok(ClientResponse.from(client));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Eliminar cliente")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        clientUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
