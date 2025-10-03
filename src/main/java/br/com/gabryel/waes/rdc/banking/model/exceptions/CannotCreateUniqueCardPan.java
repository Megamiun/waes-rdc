package br.com.gabryel.waes.rdc.banking.model.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class CannotCreateUniqueCardPan extends ResponseStatusException {
    private final int retries;

    public CannotCreateUniqueCardPan(int retries) {
        super(HttpStatusCode.valueOf(500), "Failure generating unique credit card pan over " + retries + " tries.");
        this.retries = retries;
    }
}
