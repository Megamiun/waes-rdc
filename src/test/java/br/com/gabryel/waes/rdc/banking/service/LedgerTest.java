package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.request.TransferRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.WithdrawalRequestDto;
import br.com.gabryel.waes.rdc.banking.model.entity.*;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.LedgerEntryType;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionType;
import br.com.gabryel.waes.rdc.banking.repository.LedgerEntryRepository;
import br.com.gabryel.waes.rdc.banking.repository.TransactionRepository;
import br.com.gabryel.waes.rdc.banking.repository.TransactionTransferRepository;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static br.com.gabryel.waes.rdc.banking.matchers.CustomMocks.configureRepositoryMock;
import static br.com.gabryel.waes.rdc.banking.matchers.TestConstants.MONEY_EPSILON;
import static br.com.gabryel.waes.rdc.banking.model.entity.CardType.CREDIT;
import static br.com.gabryel.waes.rdc.banking.model.entity.CardType.DEBIT;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus.COMPLETED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.captor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LedgerTest {
    private final static UUID ACCOUNT_ID = UUID.randomUUID();
    private final static UUID ACCOUNT_ID_2 = UUID.randomUUID();
    private final static UUID DEBIT_ID = UUID.randomUUID();
    private final static UUID CREDIT_ID = UUID.randomUUID();

    @Mock(strictness = LENIENT)
    private CardService cardService;

    @Mock(strictness = LENIENT)
    private AccountService accountService;

    @Mock(strictness = LENIENT)
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock(strictness = LENIENT)
    private TransactionRepository transactionRepository;

    @Mock(strictness = LENIENT)
    private TransactionTransferRepository transactionTransferRepository;

    @BeforeEach
    void setUp() {
        when(accountService.findExistingAccount(ACCOUNT_ID))
            .thenReturn(Account.builder().id(ACCOUNT_ID).build());

        when(accountService.findExistingAccount(ACCOUNT_ID_2))
            .thenReturn(Account.builder().id(ACCOUNT_ID_2).build());

        when(cardService.getCards(ACCOUNT_ID)).thenReturn(new PageImpl<>(List.of(
            AccountCard.builder().id(DEBIT_ID).type(DEBIT).build(),
            AccountCard.builder().id(CREDIT_ID).type(CREDIT).limit(new BigDecimal("100")).build()
        )));

        configureRepositoryMock(transactionTransferRepository);
        configureRepositoryMock(transactionRepository);
        configureRepositoryMock(ledgerEntryRepository);
    }

    @Test
    @DisplayName("when adding a deposit, should return a transaction")
    public void whenAddingADeposit_shouldReturnTransaction() {
        var sut = createLedger(new BigDecimal("0.01"));

        assertThat(
            sut.deposit(ACCOUNT_ID, new BigDecimal("123.45")),
            is(dbTransactionWith(ACCOUNT_ID, TransactionType.DEPOSIT, COMPLETED, 123.45, 0)));
    }

    @Test
    @DisplayName("when adding a deposit, should save a completed Transaction")
    public void whenAddingADeposit_shouldSaveACompletedTransaction() {
        var sut = createLedger(new BigDecimal("0.01"));

        sut.deposit(ACCOUNT_ID, new BigDecimal("123.45"));

        ArgumentCaptor<Transaction> accountCaptor = captor();
        verify(transactionRepository).save(accountCaptor.capture());

        assertThat(
            accountCaptor.getValue(),
            is(dbTransactionWith(ACCOUNT_ID, TransactionType.DEPOSIT, COMPLETED, 123.45, 0)));
    }

    @Test
    @DisplayName("when adding a deposit, should save deposit LedgerEntry")
    public void whenAddingADeposit_shouldSaveDepositLedgerEntry() {
        var sut = createLedger(new BigDecimal("0.01"));

        sut.deposit(ACCOUNT_ID, new BigDecimal("123.45"));

        ArgumentCaptor<LedgerEntry> accountCaptor = captor();
        verify(ledgerEntryRepository).save(accountCaptor.capture());

        assertThat(
            accountCaptor.getValue(),
            is(dbLedgerEntryWith(LedgerEntryType.DEPOSIT, ACCOUNT_ID, 123.45)));
    }

    @Test
    @DisplayName("when adding a withdrawal, should return a transaction")
    public void whenAddingAWithdrawal_shouldReturnTransaction() {
        var sut = createLedger(new BigDecimal("0.01"));

        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID))
            .thenReturn(List.of(LedgerEntry.builder().amount(new BigDecimal("100")).build()));

        assertThat(
            sut.withdraw(ACCOUNT_ID, new WithdrawalRequestDto(DEBIT_ID, new BigDecimal("10"))),
            is(dbTransactionWith(ACCOUNT_ID, TransactionType.WITHDRAWAL, COMPLETED, 10, 0)));
    }

    @Test
    @DisplayName("when adding a withdrawal, should save a completed Transaction")
    public void whenAddingAWithdrawal_shouldSaveACompletedTransaction() {
        var sut = createLedger(new BigDecimal("0.01"));

        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID))
            .thenReturn(List.of(LedgerEntry.builder().amount(new BigDecimal("100")).build()));

        sut.withdraw(ACCOUNT_ID, new WithdrawalRequestDto(DEBIT_ID, new BigDecimal("10")));

        ArgumentCaptor<Transaction> accountCaptor = captor();
        verify(transactionRepository).save(accountCaptor.capture());

        assertThat(
            accountCaptor.getAllValues(),
            contains(dbTransactionWith(ACCOUNT_ID, TransactionType.WITHDRAWAL, COMPLETED, 10, 0)));
    }

    @Test
    @DisplayName("when adding a withdrawal, should save withdrawal LedgerEntry")
    public void whenAddingAWithdrawal_shouldSaveWithdrawalLedgerEntry() {
        var sut = createLedger(new BigDecimal("0.01"));

        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID))
            .thenReturn(List.of(LedgerEntry.builder().amount(new BigDecimal("100")).build()));

        sut.withdraw(ACCOUNT_ID, new WithdrawalRequestDto(DEBIT_ID, new BigDecimal("10")));

        ArgumentCaptor<LedgerEntry> accountCaptor = captor();
        verify(ledgerEntryRepository).save(accountCaptor.capture());

        assertThat(
            accountCaptor.getAllValues().getLast(),
            is(dbLedgerEntryWith(LedgerEntryType.WITHDRAWAL, ACCOUNT_ID, -10)));
    }

    @Test
    @DisplayName("given withdrawal is done via credit, when adding a withdrawal, should save fee LedgerEntry")
    public void givenWithdrawalIsDoneViaCredit_whenAddingAWithdrawal_shouldSaveFeeLedgerEntry() {
        var sut = createLedger(new BigDecimal("0.01"));

        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID))
            .thenReturn(List.of(LedgerEntry.builder().amount(new BigDecimal("100")).build()));

        sut.withdraw(ACCOUNT_ID, new WithdrawalRequestDto(CREDIT_ID, new BigDecimal("10")));

        ArgumentCaptor<LedgerEntry> accountCaptor = captor();
        verify(ledgerEntryRepository, times(2)).save(accountCaptor.capture());

        assertThat(accountCaptor.getAllValues(), contains(
            dbLedgerEntryWith(LedgerEntryType.WITHDRAWAL, ACCOUNT_ID, -10),
            dbLedgerEntryWith(LedgerEntryType.FEE, ACCOUNT_ID, -0.1)));
    }

    @Test
    @DisplayName("given total withdrawal amount exceeds balance, when adding a withdrawal, should fail")
    public void givenTotalWithdrawalAmountExceedsBalance_whenAddingAWithdrawal_shouldSaveDepositLedgerEntry() {
        var sut = createLedger(new BigDecimal("0.5"));

        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID))
            .thenReturn(List.of(LedgerEntry.builder().amount(new BigDecimal("12")).build()));

        assertThrows(
            IllegalStateException.class,
            () -> sut.withdraw(ACCOUNT_ID, new WithdrawalRequestDto(CREDIT_ID, new BigDecimal("10"))));
    }

    @Test
    @DisplayName("when adding a transfer, should return a transaction")
    public void whenAddingATransfer_shouldReturnTransaction() {
        var sut = createLedger(new BigDecimal("0.01"));

        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID))
            .thenReturn(List.of(LedgerEntry.builder().amount(new BigDecimal("100")).build()));

        assertThat(
            sut.transfer(ACCOUNT_ID, new TransferRequestDto(ACCOUNT_ID_2, DEBIT_ID, new BigDecimal("10"))),
            is(dbTransactionWith(ACCOUNT_ID, TransactionType.TRANSFER, COMPLETED, 10, 0)));
    }

    @Test
    @DisplayName("when adding a transfer, should save a completed Transaction")
    public void whenAddingATransfer_shouldSaveACompletedTransaction() {
        var sut = createLedger(new BigDecimal("0.01"));

        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID))
            .thenReturn(List.of(LedgerEntry.builder().amount(new BigDecimal("100")).build()));

        sut.transfer(ACCOUNT_ID, new TransferRequestDto(ACCOUNT_ID_2, DEBIT_ID, new BigDecimal("10")));

        ArgumentCaptor<Transaction> accountCaptor = captor();
        verify(transactionRepository).save(accountCaptor.capture());

        assertThat(
            accountCaptor.getAllValues(),
            contains(dbTransactionWith(ACCOUNT_ID, TransactionType.TRANSFER, COMPLETED, 10, 0)));
    }

    @Test
    @DisplayName("when adding a transfer, should save a TransactionTransfer")
    public void whenAddingATransfer_shouldSaveACompletedTransactionTransfer() {
        var sut = createLedger(new BigDecimal("0.01"));

        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID))
            .thenReturn(List.of(LedgerEntry.builder().amount(new BigDecimal("100")).build()));

        sut.transfer(ACCOUNT_ID, new TransferRequestDto(ACCOUNT_ID_2, DEBIT_ID, new BigDecimal("10")));

        ArgumentCaptor<TransactionTransfer> accountCaptor = captor();
        verify(transactionTransferRepository).save(accountCaptor.capture());

        assertThat(
            accountCaptor.getAllValues(),
            contains(hasFeature("receiverId", transfer -> transfer.getReceiver().getId(), equalTo(ACCOUNT_ID_2))));
    }

    @Test
    @DisplayName("when adding a transfer, should save transfer LedgerEntry")
    public void whenAddingATransfer_shouldSaveTransferLedgerEntry() {
        var sut = createLedger(new BigDecimal("0.01"));

        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID))
            .thenReturn(List.of(LedgerEntry.builder().amount(new BigDecimal("100")).build()));

        sut.transfer(ACCOUNT_ID, new TransferRequestDto(ACCOUNT_ID_2, DEBIT_ID, new BigDecimal("10")));

        ArgumentCaptor<LedgerEntry> accountCaptor = captor();
        verify(ledgerEntryRepository, times(2)).save(accountCaptor.capture());

        assertThat(accountCaptor.getAllValues(), contains(
            dbLedgerEntryWith(LedgerEntryType.TRANSFER_SENT, ACCOUNT_ID, -10),
            dbLedgerEntryWith(LedgerEntryType.TRANSFER_RECEIVED, ACCOUNT_ID_2, 10)));
    }

    @Test
    @DisplayName("given transfer is done via credit, when adding a transfer, should save fee LedgerEntry")
    public void givenTransferIsDoneViaCredit_whenAddingAWithdrawal_shouldSaveFeeLedgerEntry() {
        var sut = createLedger(new BigDecimal("0.01"));

        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID))
            .thenReturn(List.of(LedgerEntry.builder().amount(new BigDecimal("100")).build()));

        sut.transfer(ACCOUNT_ID, new TransferRequestDto(ACCOUNT_ID_2, CREDIT_ID, new BigDecimal("10")));

        ArgumentCaptor<LedgerEntry> accountCaptor = captor();
        verify(ledgerEntryRepository, times(3)).save(accountCaptor.capture());

        assertThat(accountCaptor.getAllValues(), contains(
            dbLedgerEntryWith(LedgerEntryType.TRANSFER_SENT, ACCOUNT_ID, -10),
            dbLedgerEntryWith(LedgerEntryType.TRANSFER_RECEIVED, ACCOUNT_ID_2, 10),
            dbLedgerEntryWith(LedgerEntryType.FEE, ACCOUNT_ID, -0.1)));
    }

    @Test
    @DisplayName("given total transfer amount exceeds balance, when adding a transfer, should fail")
    public void givenTotalTransferAmountExceedsBalance_whenAddingAWithdrawal_shouldSaveDepositLedgerEntry() {
        var sut = createLedger(new BigDecimal("0.5"));

        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID))
            .thenReturn(List.of(LedgerEntry.builder().amount(new BigDecimal("12")).build()));

        assertThrows(
            IllegalStateException.class,
            () -> sut.transfer(ACCOUNT_ID, new TransferRequestDto(ACCOUNT_ID_2, CREDIT_ID, new BigDecimal("10"))));
    }

    @Test
    @DisplayName("when getting an account balance, should sum together all ledger items")
    public void whenGettingAnAccountBalance_shouldSumTogetherAllLedgerItems() {
        when(ledgerEntryRepository.findByAccountId(ACCOUNT_ID)).thenReturn(List.of(
            ledgerEntryWithAmount(ACCOUNT_ID, "123.45"),
            ledgerEntryWithAmount(ACCOUNT_ID, "76.54"),
            ledgerEntryWithAmount(ACCOUNT_ID, "-10")
        ));

        var sut = createLedger(new BigDecimal("0.01"));
        var balance = sut.getBalance(ACCOUNT_ID);

        assertThat(balance, closeTo(new BigDecimal("189.99"), MONEY_EPSILON));
    }

    @Test
    @DisplayName("when getting a page of account balances, should sum together all ledger items for all given accounts")
    public void whenGettingAPageOdAccountBalances_shouldSumTogetherAllLedgerItemsForGivenAccounts() {
        var altAccountID = UUID.randomUUID();

        when(ledgerEntryRepository.findByAccountIdIn(List.of(ACCOUNT_ID, altAccountID))).thenReturn(List.of(
            ledgerEntryWithAmount(ACCOUNT_ID, "123.45"),
            ledgerEntryWithAmount(ACCOUNT_ID, "76.54"),
            ledgerEntryWithAmount(altAccountID, "10"),
            ledgerEntryWithAmount(altAccountID, "-5")
        ));

        var sut = createLedger(new BigDecimal("0.01"));
        var balances = sut.getBalances(List.of(ACCOUNT_ID, altAccountID), null, null);

        assertThat(balances, containsInAnyOrder(
            dtoAccountBalancePair(ACCOUNT_ID, 199.99),
            dtoAccountBalancePair(altAccountID, 5.00)
        ));
    }

    @Test
    @DisplayName("when getting a page of account balances, should paginate it correctly")
    public void whenGettingAPageOfAccountBalances_shouldPaginateItCorrectly() {
        var accounts = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        var sut = createLedger(new BigDecimal("0.01"));
        var balances = sut.getBalances(accounts, 3, 1);

        assertAll(
            () -> assertThat("Total Elements", balances.getTotalElements(), equalTo(5L)),
            () -> assertThat("Elements on page", balances.getContent(), hasSize(2)),
            () -> assertThat("Page Size", balances.getPageable().getPageSize(), equalTo(3)),
            () -> assertThat("Page Number", balances.getPageable().getPageNumber(), equalTo(1))
        );
    }

    @Test
    @DisplayName("given no account ids are given, when getting a page of account balances, should get accountIds from DB")
    public void givenNoAccountIdsAreGiven_whenGettingAPageOfAccountBalances_shouldPaginateItCorrectly() {
        var accountIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        var accounts = accountIds.stream()
            .map(id -> Account.builder().id(id).build())
            .toList();

        var pageable = Pageable.ofSize(2).withPage(2);
        var page = new PageImpl<>(accounts, pageable, 2000);
        when(accountService.getAccounts(any())).thenReturn(page);

        var sut = createLedger(new BigDecimal("0.01"));
        var balances = sut.getBalances(null, 2, 2);

        assertThat(balances.getContent().stream().map(Pair::getFirst).toList(), contains(accountIds.toArray()));
    }

    @Test
    @DisplayName("given no account ids are given, when getting a page of account balances, should get accountIds from DB")
    public void givenEmptyAccountIdsAreGiven_whenGettingAPageOfAccountBalances_shouldPaginateItCorrectly() {
        var accountIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        var accounts = accountIds.stream()
            .map(id -> Account.builder().id(id).build())
            .toList();

        var pageable = Pageable.ofSize(2).withPage(2);
        var page = new PageImpl<>(accounts, pageable, 2000);
        when(accountService.getAccounts(any())).thenReturn(page);

        var sut = createLedger(new BigDecimal("0.01"));
        var balances = sut.getBalances(List.of(), 2, 2);

        assertThat(balances.getContent().stream().map(Pair::getFirst).toList(), contains(accountIds.toArray()));
    }

    @Test
    @DisplayName("given a non existent Account, when adding a deposit, should throw an error")
    public void givenAnNonExistentAccount_whenAddingADeposit_shouldThrowAnError() {
        var sut = createLedger(new BigDecimal("0.01"));

        when(accountService.findExistingAccount(any())).thenThrow(new IllegalArgumentException());

        assertThrows(
            IllegalArgumentException.class, // TODO Change to an specific Exception later
            () -> sut.deposit(UUID.randomUUID(), new BigDecimal("123.45"))
        );
    }

    private Ledger createLedger(BigDecimal creditCardFee) {
        return new Ledger(cardService, accountService, transactionRepository, transactionTransferRepository, ledgerEntryRepository, creditCardFee);
    }

    private static LedgerEntry ledgerEntryWithAmount(UUID accountId, String amount) {
        var account = Account.builder().id(accountId).build();
        return LedgerEntry.builder().account(account).amount(new BigDecimal(amount)).build();
    }

    public static Matcher<Transaction> dbTransactionWith(UUID accountId, TransactionType type, TransactionStatus status, double amount, double feeAmount) {
        return allOf(
            hasFeature("ownerId", transaction -> transaction.getOwner().getId(), equalTo(accountId)),
            hasFeature("amount", Transaction::getAmount, closeTo(BigDecimal.valueOf(amount), MONEY_EPSILON)),
            hasFeature("feeAmount", Transaction::getFeeAmount, closeTo(BigDecimal.valueOf(feeAmount), MONEY_EPSILON)),
            hasFeature("type", Transaction::getType, equalTo(type)),
            hasFeature("status", Transaction::getStatus, equalTo(status))
        );
    }

    public static Matcher<LedgerEntry> dbLedgerEntryWith(LedgerEntryType type, UUID accountId, double amount) {
        return allOf(
            hasFeature("accountId", entry -> entry.getAccount().getId(), equalTo(accountId)),
            hasFeature("amount", LedgerEntry::getAmount, closeTo(BigDecimal.valueOf(amount), MONEY_EPSILON)),
            hasFeature("type", LedgerEntry::getType, equalTo(type))
        );
    }

    public static Matcher<Pair<UUID, BigDecimal>> dtoAccountBalancePair(UUID accountId, Double balance) {
        return allOf(
            hasFeature("accountId", Pair::getFirst, equalTo(accountId)),
            hasFeature("balance", Pair::getSecond, closeTo(BigDecimal.valueOf(balance), MONEY_EPSILON))
        );
    }
}
