package br.com.gabryel.waes.rdc.banking.model.exceptions;

import br.com.gabryel.waes.rdc.banking.model.entity.CardType;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Getter
public class RepeatedCardTypeForAccount extends ResponseStatusException {
    private final UUID accountId;
    private final CardType cardType;

    public RepeatedCardTypeForAccount(UUID accountId, CardType cardType) {
        super(HttpStatusCode.valueOf(400), "Account with id " + accountId + " already has card with type " + cardType);
        this.accountId = accountId;
        this.cardType = cardType;
    }
}
