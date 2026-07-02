package com.renault.oude.portfolio;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class PortefeuilleIndisponibleException extends RuntimeException {
    public PortefeuilleIndisponibleException(String message) {
        super(message);
    }
}
