package br.com.gabryel.waes.rdc.banking.controller;

import br.com.gabryel.waes.rdc.banking.controller.dto.*;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.*;
import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountDocument;
import br.com.gabryel.waes.rdc.banking.service.AccountService;
import br.com.gabryel.waes.rdc.banking.service.Ledger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private final Ledger ledger;

    // Created out of spec, for simplicity/testing
    @PutMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody CreateAccountRequestDto request) {
        var account = accountService.saveAccount(request);
        var documents = accountService.getAccountDocuments(account.getId());

        return ResponseEntity
            .created(URI.create("/accounts/" + account.getId()))
            .body(mapToDto(account, documents));
    }

    @GetMapping("/{id}")
    public AccountDto getAccount(@PathVariable("id") UUID id) {
        var account = accountService.getAccount(id);
        var documents = accountService.getAccountDocuments(id);

        return mapToDto(account, documents);
    }

    // In-spec methods
    @GetMapping("/{accountId}/balance")
    public BalanceDto getAccountBalance(@PathVariable("accountId") UUID accountId) {
        return new BalanceDto(ledger.getBalance(accountId));
    }

    private static AccountDto mapToDto(Account mapped, List<AccountDocument> documents) {
        var documentDtos = documents.stream()
            .map(doc -> new DocumentDto(doc.getType(), doc.getNumber()))
            .toList();

        return new AccountDto(mapped.getId(), mapped.getName(), mapped.getSurname(), documentDtos);
    }
}
