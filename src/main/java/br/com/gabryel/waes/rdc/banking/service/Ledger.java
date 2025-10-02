package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.LedgerEntry;
import br.com.gabryel.waes.rdc.banking.model.entity.Transaction;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.LedgerEntryType;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionMethod;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionType;
import br.com.gabryel.waes.rdc.banking.repository.AccountRepository;
import br.com.gabryel.waes.rdc.banking.repository.LedgerEntryRepository;
import br.com.gabryel.waes.rdc.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionMethod.ATM;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus.COMPLETED;
import static java.math.BigDecimal.ZERO;

@Service
@RequiredArgsConstructor
public class Ledger {

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public Transaction deposit(UUID accountId, BigDecimal amount) {
        var account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account with id " + accountId + " not found"));

        var transaction = transactionRepository.save(createTransaction(account, amount, TransactionType.DEPOSIT, ATM, COMPLETED));

        ledgerEntryRepository.save(createLedgerEntry(account, transaction, amount, LedgerEntryType.DEPOSIT));

        return transaction;
    }

    public BigDecimal getBalance(UUID id) {
        return ledgerEntryRepository.findByAccountId(id).stream()
            .map(LedgerEntry::getAmount)
            .reduce(ZERO, BigDecimal::add);
    }

    private static LedgerEntry createLedgerEntry(Account account, Transaction transaction, BigDecimal amount, LedgerEntryType type) {
        return LedgerEntry.builder()
            .type(type)
            .account(account)
            .transaction(transaction)
            .amount(amount)
            .build();
    }

    private static Transaction createTransaction(Account account, BigDecimal amount, TransactionType type, TransactionMethod method, TransactionStatus status) {
        return Transaction.builder()
            .method(method)
            .type(type)
            .feeAmount(BigDecimal.ZERO)
            .status(status)
            .owner(account)
            .amount(amount)
            .build();
    }
}
