package com.btg.fondos.domain.exception;

public class NotSubscribedException extends RuntimeException {

    public NotSubscribedException(String fundName) {
        super("No se encuentra suscrito al fondo " + fundName);
    }
}
