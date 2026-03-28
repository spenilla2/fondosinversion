package com.btg.fondos.domain.port.in;

import com.btg.fondos.domain.model.Client;

import java.util.List;

public interface ClientUseCase {

    Client create(String user, String name, String email, String phone, String password, String preferredNotification);

    Client update(String id, String name, String phone, String preferredNotification);

    void delete(String id);

    Client getById(String id);

    Client getByUser(String user);

    List<Client> getAll();
}
