package br.com.gabryel.waes.rdc.banking.controller;

import br.com.gabryel.waes.rdc.banking.controller.dto.*;
import br.com.gabryel.waes.rdc.banking.model.Account;
import br.com.gabryel.waes.rdc.banking.model.AccountDocument;
import br.com.gabryel.waes.rdc.banking.service.AccountService;
import jakarta.websocket.server.PathParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Created out of spec, for simplicity/testing
    @PutMapping
    public AccountDto createAccount(@RequestBody CreateAccountRequestDto request) {
        var account = accountService.saveAccount(request);
        var documents = accountService.saveDocuments(request, account.id());

        return map(account, documents);
    }

    @GetMapping("/{id}")
    public AccountDto getAccount(@PathParam("id") UUID id) {
        var account = accountService.getAccount(id);
        var documents = accountService.getAccountDocuments(id);

        return map(account, documents);
    }

    // In-spec methods
    @GetMapping("/{id}")
    public Double getAccountBalance(@PathParam("id") UUID id) {
        throw new UnsupportedOperationException("TODO Get account balance for " + id);
    }

    @PutMapping("/{senderId}/transfers")
    public Boolean transfer(@PathParam("senderId") UUID senderId, @RequestBody TransferRequestDto request) {
        throw new UnsupportedOperationException("TODO Transfer amount from " + senderId);
    }

    @PutMapping("/{id}/withdrawal")
    public Boolean requestWithdrawal(@PathParam("id") UUID id, @RequestBody WithdrawalRequestDto request) {
        throw new UnsupportedOperationException("TODO Withdraw amount for " + id);
    }

    private static AccountDto map(Account mapped, List<AccountDocument> documents) {
        var documentDtos = documents.stream()
            .map(doc -> new DocumentDto(doc.documentType(), doc.number()))
            .toList();

        return new AccountDto(mapped.id(), mapped.name(), mapped.surname(), documentDtos);
    }
}
