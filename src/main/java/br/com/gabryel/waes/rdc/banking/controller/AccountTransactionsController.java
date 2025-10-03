package br.com.gabryel.waes.rdc.banking.controller;

import br.com.gabryel.waes.rdc.banking.controller.dto.TransactionDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.DepositRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.TransferRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.WithdrawalRequestDto;
import br.com.gabryel.waes.rdc.banking.model.entity.Transaction;
import br.com.gabryel.waes.rdc.banking.service.Ledger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class AccountTransactionsController {

    private final Ledger ledger;

    // Created out of spec, for simplicity/testing
    @GetMapping("/{transactionId}")
    public TransactionDto getTransaction(@PathVariable("accountId") UUID accountId, @PathVariable("transactionId") UUID transactionId) {
        return mapToDto(ledger.getTransaction(accountId, transactionId));
    }

    @PutMapping("/deposits")
    public ResponseEntity<TransactionDto> deposit(@PathVariable("accountId") UUID accountId, @RequestBody DepositRequestDto request) {
        var transaction = ledger.deposit(accountId, request.amount());

        return ResponseEntity
            .created(URI.create("/accounts/" + accountId + "/transactions/" + transaction.getId()))
            .body(mapToDto(transaction));
    }

    // In-spec methods
    @PutMapping("/transfers")
    public ResponseEntity<TransactionDto> transfer(@PathVariable("accountId") UUID accountId, @RequestBody TransferRequestDto request) {
        var transaction = ledger.transfer(accountId, request);

        return ResponseEntity
            .created(URI.create("/accounts/" + accountId + "/transactions/" + transaction.getId()))
            .body(mapToDto(transaction));
    }

    @PutMapping("/withdrawals")
    public ResponseEntity<TransactionDto> withdraw(@PathVariable("accountId") UUID accountId, @RequestBody WithdrawalRequestDto request) {
        var transaction = ledger.withdraw(accountId, request);

        return ResponseEntity
            .created(URI.create("/accounts/" + accountId + "/transactions/" + transaction.getId()))
            .body(mapToDto(transaction));
    }

    private static TransactionDto mapToDto(Transaction transaction) {
        return new TransactionDto(transaction.getId(), transaction.getStatus(), transaction.getAmount(), transaction.getFeeAmount());
    }
}
