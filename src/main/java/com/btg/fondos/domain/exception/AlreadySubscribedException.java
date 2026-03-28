package com.btg.fondos.domain.exception;

public class AlreadySubscribedException extends RuntimeException {

    public AlreadySubscribedException(String fundName) {
        super("Ya se encuentra suscrito al fondo " + fundName);
    }
}
