package com.btg.fondos.domain.port.out;

import com.btg.fondos.domain.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {

    Optional<Client> findById(String id);

    Optional<Client> findByEmail(String email);

    Optional<Client> findByUser(String user);

    Client save(Client client);

    void deleteById(String id);

    List<Client> findAll();
}
