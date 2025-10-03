package br.com.gabryel.waes.rdc.banking.model.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Getter
public class NonExistentCardForAccount extends ResponseStatusException {
    private final UUID accountId;
    private final UUID cardId;

    public NonExistentCardForAccount(UUID accountId, UUID cardId) {
        super(HttpStatusCode.valueOf(400), "Account with id " + accountId + " has no card with id " + cardId);
        this.accountId = accountId;
        this.cardId = cardId;
    }
}
