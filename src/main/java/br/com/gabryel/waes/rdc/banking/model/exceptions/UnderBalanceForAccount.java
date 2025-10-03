package br.com.gabryel.waes.rdc.banking.model.exceptions;

import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class UnderBalanceForAccount extends ResponseStatusException {
    private final UUID accountId;
    private final BigDecimal requiredBalance;

    public UnderBalanceForAccount( UUID accountId, BigDecimal requiredBalance) {
        super(HttpStatusCode.valueOf(400), "Account " + accountId + " balance is below required amount of " + requiredBalance);
        this.accountId = accountId;
        this.requiredBalance = requiredBalance;
    }
}
