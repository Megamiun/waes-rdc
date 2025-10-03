package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.request.WithdrawalRequestDto;
import br.com.gabryel.waes.rdc.banking.model.entity.*;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.LedgerEntryType;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionMethod;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionType;
import br.com.gabryel.waes.rdc.banking.repository.LedgerEntryRepository;
import br.com.gabryel.waes.rdc.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static br.com.gabryel.waes.rdc.banking.helper.NullUtils.firstNonNull;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionMethod.ATM;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus.COMPLETED;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.groupingBy;

@Service
public class Ledger {

    private final CardService cardService;

    private final AccountService accountService;

    private final TransactionRepository transactionRepository;

    private final LedgerEntryRepository ledgerEntryRepository;
    private final BigDecimal creditCardFee;

    public Ledger(
        CardService cardService,
        AccountService accountService,
        TransactionRepository transactionRepository,
        LedgerEntryRepository ledgerEntryRepository,
        @Value("${app.transaction.fee.credit}")
        BigDecimal creditCardFee
    ) {
        this.cardService = cardService;
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.creditCardFee = creditCardFee;
    }

    @Transactional
    public Transaction deposit(UUID accountId, BigDecimal amount) {
        var account = accountService.findExistingAccount(accountId);
        var transaction = transactionRepository.save(
            createTransaction(account, amount, TransactionType.DEPOSIT, ATM, null, ZERO));

        ledgerEntryRepository.save(createLedgerEntry(account, transaction, amount, LedgerEntryType.DEPOSIT));

        return transaction;
    }

    @Transactional
    public Transaction withdraw(UUID accountId, WithdrawalRequestDto request) {
        var account = accountService.findExistingAccount(accountId);
        var chosenCard = getChosenCard(accountId, request, account);
        var fee = calculateTransactionFee(request.amount(), chosenCard.getType());

        validateBalance(accountId, request, fee);

        var transaction = transactionRepository.save(
            createTransaction(
                account,
                request.amount(),
                TransactionType.WITHDRAWAL,
                chosenCard.getType().asTransactionMethod(),
                chosenCard,
                fee
            )
        );

        ledgerEntryRepository.save(createLedgerEntry(account, transaction, request.amount().negate(), LedgerEntryType.WITHDRAWAL));

        if (!fee.equals(ZERO))
            ledgerEntryRepository.save(createLedgerEntry(account, transaction, fee.negate(), LedgerEntryType.FEE));

        return transaction;
    }

    public BigDecimal getBalance(UUID id) {
        return getBalance(ledgerEntryRepository.findByAccountId(id));
    }

    public Page<Pair<UUID, BigDecimal>> getBalances(List<UUID> accountIds, Integer pageSize, Integer pageNumber) {
        var page = getAccountPage(accountIds, pageSize, pageNumber);
        var entries = ledgerEntryRepository.findByAccountIdIn(accountIds).stream()
            .collect(groupingBy((entry) -> entry.getAccount().getId()));

        return page.map(accountId -> Pair.of(
            accountId,
            getBalance(entries.getOrDefault(accountId, List.of()))));
    }

    private Page<UUID> getAccountPage(List<UUID> accountIds, Integer pageSize, Integer pageNumber) {
        var selectedPageNumber = firstNonNull(pageNumber, 0);

        if (accountIds == null || accountIds.isEmpty()) {
            var pageable = PageRequest.of(selectedPageNumber, firstNonNull(pageSize, 100));
            return accountService.getAccounts(pageable).map(Account::getId);
        }

        var pageable = PageRequest.of(selectedPageNumber, firstNonNull(pageSize, accountIds.size()));
        var accountsPage = accountIds.stream()
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize())
            .toList();
        return new PageImpl<>(accountsPage, pageable, accountIds.size());
    }

    private static BigDecimal getBalance(List<LedgerEntry> entries) {
        return entries.stream()
            .map(LedgerEntry::getAmount)
            .reduce(ZERO, BigDecimal::add);
    }

    private AccountCard getChosenCard(UUID accountId, WithdrawalRequestDto request, Account account) {
        return cardService.getCards(account.getId()).stream()
            .filter(card -> card.getId() == request.cardId())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Card with id " + request.cardId() + " not found on account with id " + accountId));
    }

    private BigDecimal calculateTransactionFee(BigDecimal amount, CardType type) {
        if (type == CardType.CREDIT)
            return amount.multiply(creditCardFee);

        return ZERO;
    }

    private void validateBalance(UUID accountId, WithdrawalRequestDto request, BigDecimal fee) {
        var total = request.amount().add(fee);
        var balance = getBalance(accountId);

        if (total.compareTo(balance) > 0)
            throw new IllegalStateException("Account " + accountId + " balance is below requested amount");
    }

    private static LedgerEntry createLedgerEntry(Account account, Transaction transaction, BigDecimal amount, LedgerEntryType type) {
        return LedgerEntry.builder()
            .type(type)
            .account(account)
            .transaction(transaction)
            .amount(amount)
            .build();
    }

    private static Transaction createTransaction(
        Account account,
        BigDecimal amount,
        TransactionType type,
        TransactionMethod method,
        AccountCard card,
        BigDecimal fee
    ) {
        return Transaction.builder()
            .method(method)
            .type(type)
            .card(card)
            .feeAmount(fee)
            // Always COMPLETED as currently we are only doing synchronous operations
            // PENDING could be used for asynchronous operations, such as CREDIT_CARD authorizations, antifraud, etc
            .status(COMPLETED)
            .owner(account)
            .amount(amount)
            .build();
    }
}
