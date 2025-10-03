package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.request.TransferRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.WithdrawalRequestDto;
import br.com.gabryel.waes.rdc.banking.model.entity.*;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.LedgerEntryType;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionMethod;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionType;
import br.com.gabryel.waes.rdc.banking.model.exceptions.NonExistentTransactionForAccount;
import br.com.gabryel.waes.rdc.banking.model.exceptions.UnderBalanceForAccount;
import br.com.gabryel.waes.rdc.banking.repository.LedgerEntryRepository;
import br.com.gabryel.waes.rdc.banking.repository.TransactionRepository;
import br.com.gabryel.waes.rdc.banking.repository.TransactionTransferRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static br.com.gabryel.waes.rdc.banking.helper.NullUtils.firstNonNull;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.LedgerEntryType.*;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionMethod.ATM;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus.COMPLETED;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.stream.Collectors.groupingBy;

@Service
public class Ledger {

    private final CardService cardService;

    private final AccountService accountService;

    private final TransactionRepository transactionRepository;

    private final TransactionTransferRepository transactionTransferRepository;

    private final LedgerEntryRepository ledgerEntryRepository;
    private final BigDecimal creditCardFee;

    public Ledger(
        CardService cardService,
        AccountService accountService,
        TransactionRepository transactionRepository,
        TransactionTransferRepository transactionTransferRepository,
        LedgerEntryRepository ledgerEntryRepository,
        @Value("${app.transaction.fee.credit}")
        BigDecimal creditCardFee
    ) {
        this.cardService = cardService;
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.transactionTransferRepository = transactionTransferRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.creditCardFee = creditCardFee;
    }

    @Transactional
    public Transaction deposit(UUID accountId, BigDecimal amount) {
        var account = accountService.findExistingAccount(accountId);
        var transaction = transactionRepository.save(
            createTransactionEntity(account, amount, TransactionType.DEPOSIT, ATM, null, ZERO));

        ledgerEntryRepository.save(createLedgerEntry(account, transaction, transaction.getAmount(), LedgerEntryType.DEPOSIT));

        return transaction;
    }

    @Transactional
    public Transaction withdraw(UUID accountId, WithdrawalRequestDto request) {
        var account = accountService.findExistingAccount(accountId);
        var transaction = createTransaction(account, TransactionType.WITHDRAWAL, request.cardId(), request.amount());

        ledgerEntryRepository.save(createLedgerEntry(account, transaction, transaction.getAmount().negate(), LedgerEntryType.WITHDRAWAL));

        if (transaction.getFeeAmount().compareTo(ZERO) != 0)
            ledgerEntryRepository.save(createLedgerEntry(account, transaction, transaction.getFeeAmount().negate(), FEE));

        return transaction;
    }

    @Transactional
    public Transaction transfer(UUID accountId, TransferRequestDto request) {
        var account = accountService.findExistingAccount(accountId);
        var receiverAccount = accountService.findExistingAccount(request.receiverAccountId());

        var transaction = createTransaction(account, TransactionType.TRANSFER, request.cardId(), request.amount());

        var transfer = TransactionTransfer.builder().transaction(transaction).receiver(receiverAccount).build();
        transactionTransferRepository.save(transfer);

        ledgerEntryRepository.save(createLedgerEntry(account, transaction, transaction.getAmount().negate(), TRANSFER_SENT));
        ledgerEntryRepository.save(createLedgerEntry(receiverAccount, transaction, transaction.getAmount(), TRANSFER_RECEIVED));

        if (transaction.getFeeAmount().compareTo(ZERO) != 0)
            ledgerEntryRepository.save(createLedgerEntry(account, transaction, transaction.getFeeAmount().negate(), FEE));

        return transaction;
    }

    private Transaction createTransaction(Account account, TransactionType transactionType, UUID cardId, BigDecimal amount) {
        var accountId = account.getId();

        var chosenCard = cardService.getExistentCard(accountId, cardId);
        var scaledAmount = scaleToMoney(amount);
        var fee = calculateTransactionFee(scaledAmount, chosenCard.getType());

        validateBalance(accountId, scaledAmount, fee);

        return transactionRepository.save(
            createTransactionEntity(
                account,
                scaledAmount,
                transactionType,
                chosenCard.getType().asTransactionMethod(),
                chosenCard,
                fee
            )
        );
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

    public Transaction getTransaction(UUID accountId, UUID transactionId) {
        return transactionRepository.findByOwnerIdAndId(accountId, transactionId)
            .orElseThrow(() -> new NonExistentTransactionForAccount(accountId, transactionId));
    }

    public Page<Transaction> getTransactions(List<UUID> accountIds, List<TransactionType> types, Integer pageSize, Integer pageNumber) {
        var page = Pageable.ofSize(firstNonNull(pageSize, 100)).withPage(firstNonNull(pageNumber, 0));
        return transactionRepository.findByOwnerIdInAndTypeIn(accountIds, types, page);
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

    private BigDecimal calculateTransactionFee(BigDecimal amount, CardType type) {
        if (type == CardType.CREDIT)
            return scaleToMoney(amount.multiply(creditCardFee));

        return ZERO;
    }

    private void validateBalance(UUID accountId, BigDecimal amount, BigDecimal fee) {
        var total = amount.add(fee);
        var balance = getBalance(accountId);

        if (total.compareTo(balance) > 0)
            throw new UnderBalanceForAccount(accountId, total);
    }

    private static LedgerEntry createLedgerEntry(Account account, Transaction transaction, BigDecimal amount, LedgerEntryType type) {
        return LedgerEntry.builder()
            .type(type)
            .account(account)
            .transaction(transaction)
            .amount(amount)
            .build();
    }

    private static Transaction createTransactionEntity(
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
            // Always COMPLETED as currently we are only doing synchronous operations
            // PENDING could be used for asynchronous operations, such as CREDIT_CARD authorizations, antifraud, etc
            .status(COMPLETED)
            .owner(account)
            .amount(amount.setScale(2, FLOOR))
            .feeAmount(fee.setScale(2, FLOOR))
            .build();
    }

    private static BigDecimal scaleToMoney(BigDecimal amount) {
        return amount.setScale(2, HALF_EVEN);
    }
}
