package br.com.gabryel.waes.rdc.banking.controller;

import br.com.gabryel.waes.rdc.banking.controller.dto.*;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.CreateAccountRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.DepositRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.TransferRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.WithdrawalRequestDto;
import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountDocument;
import br.com.gabryel.waes.rdc.banking.model.entity.Transaction;
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

    @PutMapping("/{id}/transactions/deposits")
    public ResponseEntity<TransactionDto> deposit(@PathVariable("id") UUID id, @RequestBody DepositRequestDto request) {
        var transaction = ledger.deposit(id, request.amount());

        return ResponseEntity
            .created(URI.create("/accounts/" + id + "/transactions/" + transaction.getId()))
            .body(mapToDto(transaction));
    }

    @GetMapping("/{accountId}/transactions/{transactionId}")
    public TransactionDto getTransaction(@PathVariable("accountId") UUID accountId, @PathVariable("transactionId") UUID transactionId) {
        throw new UnsupportedOperationException("TODO Get transaction  " + transactionId);
    }

    // In-spec methods
    @GetMapping("/{id}/balance")
    public BalanceDto getAccountBalance(@PathVariable("id") UUID id) {
        return new BalanceDto(ledger.getBalance(id));
    }

    @PutMapping("/{senderId}/transactions/transfers")
    public Boolean transfer(@PathVariable("senderId") UUID senderId, @RequestBody TransferRequestDto request) {
        throw new UnsupportedOperationException("TODO Transfer amount from " + senderId);
    }

    @PutMapping("/{id}/transactions/withdrawals")
    public Boolean withdraw(@PathVariable("id") UUID id, @RequestBody WithdrawalRequestDto request) {
        throw new UnsupportedOperationException("TODO Withdraw amount from " + id);
    }

    private static AccountDto mapToDto(Account mapped, List<AccountDocument> documents) {
        var documentDtos = documents.stream()
            .map(doc -> new DocumentDto(doc.getType(), doc.getNumber()))
            .toList();

        return new AccountDto(mapped.getId(), mapped.getName(), mapped.getSurname(), documentDtos);
    }

    private static TransactionDto mapToDto(Transaction transaction) {
        return new TransactionDto(transaction.getId(), transaction.getStatus());
    }
}
