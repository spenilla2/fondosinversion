package com.btg.fondos.domain.port.in;

public interface AuthUseCase {

    String authenticate(String email, String password);
}
