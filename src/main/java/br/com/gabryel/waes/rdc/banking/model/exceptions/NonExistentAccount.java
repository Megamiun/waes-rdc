package br.com.gabryel.waes.rdc.banking.model.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Getter
public class NonExistentAccount extends ResponseStatusException {
    private final UUID accountId;

    public NonExistentAccount(UUID accountId) {
        super(HttpStatusCode.valueOf(400), "Account with id " + accountId + " not found");
        this.accountId = accountId;
    }
}
