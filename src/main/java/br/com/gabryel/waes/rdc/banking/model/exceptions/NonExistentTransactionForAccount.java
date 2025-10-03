package br.com.gabryel.waes.rdc.banking.model.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Getter
public class NonExistentTransactionForAccount extends ResponseStatusException {
    private final UUID accountId;
    private final UUID transactionId;

    public NonExistentTransactionForAccount(UUID accountId, UUID transactionId) {
        super(HttpStatusCode.valueOf(400), "Account with id " + accountId + " has no transaction with id " + transactionId);
        this.accountId = accountId;
        this.transactionId = transactionId;
    }
}
