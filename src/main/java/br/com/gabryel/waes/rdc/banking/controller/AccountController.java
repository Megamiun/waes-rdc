package br.com.gabryel.waes.rdc.banking.controller;

import br.com.gabryel.waes.rdc.banking.controller.dto.*;
import br.com.gabryel.waes.rdc.banking.model.Account;
import br.com.gabryel.waes.rdc.banking.model.AccountDocument;
import br.com.gabryel.waes.rdc.banking.service.AccountService;
import jakarta.websocket.server.PathParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Created out of spec, for simplicity/testing
    @PutMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody CreateAccountRequestDto request) {
        var account = accountService.saveAccount(request);
        var documents = accountService.getAccountDocuments(account.getId());

        return ResponseEntity
            .created(URI.create("/accounts/" + account.getId()))
            .body(map(account, documents));
    }

    @GetMapping("/{id}")
    public AccountDto getAccount(@PathParam("id") UUID id) {
        var account = accountService.getAccount(id);
        var documents = accountService.getAccountDocuments(id);

        return map(account, documents);
    }

    @PutMapping("/{id}/deposits")
    public Boolean deposit(@PathParam("id") UUID id, @RequestBody DepositRequestDto request) {
        throw new UnsupportedOperationException("TODO Deposit amount to " + id);
    }

    // In-spec methods
    @GetMapping("/{id}/balance")
    public Double getAccountBalance(@PathParam("id") UUID id) {
        throw new UnsupportedOperationException("TODO Get account balance for " + id);
    }

    @PutMapping("/{senderId}/transfers")
    public Boolean transfer(@PathParam("senderId") UUID senderId, @RequestBody TransferRequestDto request) {
        throw new UnsupportedOperationException("TODO Transfer amount from " + senderId);
    }

    @PutMapping("/{id}/withdrawals")
    public Boolean withdraw(@PathParam("id") UUID id, @RequestBody WithdrawalRequestDto request) {
        throw new UnsupportedOperationException("TODO Withdraw amount from " + id);
    }

    private static AccountDto map(Account mapped, List<AccountDocument> documents) {
        var documentDtos = documents.stream()
            .map(doc -> new DocumentDto(doc.getType(), doc.getNumber()))
            .toList();

        return new AccountDto(mapped.getId(), mapped.getName(), mapped.getSurname(), documentDtos);
    }
}
