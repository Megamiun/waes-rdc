package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.LedgerEntry;
import br.com.gabryel.waes.rdc.banking.model.entity.Transaction;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.LedgerEntryType;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionType;
import br.com.gabryel.waes.rdc.banking.repository.AccountRepository;
import br.com.gabryel.waes.rdc.banking.repository.LedgerEntryRepository;
import br.com.gabryel.waes.rdc.banking.repository.TransactionRepository;
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

import static br.com.gabryel.waes.rdc.banking.matchers.CustomDbMatchers.*;
import static br.com.gabryel.waes.rdc.banking.matchers.CustomDtoMatchers.dtoAccountBalancePair;
import static br.com.gabryel.waes.rdc.banking.matchers.CustomMocks.configureRepositoryMock;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus.COMPLETED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.captor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LedgerTest {
    private final static UUID DEFAULT_ACCOUNT_ID = UUID.randomUUID();

    @Mock(strictness = LENIENT)
    private AccountRepository accountRepository;

    @Mock(strictness = LENIENT)
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock(strictness = LENIENT)
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        configureRepositoryMock(accountRepository);

        accountRepository.save(Account.builder().id(DEFAULT_ACCOUNT_ID).build());

        configureRepositoryMock(transactionRepository);
        configureRepositoryMock(ledgerEntryRepository);
    }

    @Test
    @DisplayName("when adding a deposit, should return a transaction")
    public void whenAddingADeposit_shouldReturnTransaction() {
        var sut = new Ledger(accountRepository, transactionRepository, ledgerEntryRepository);

        assertThat(
            sut.deposit(DEFAULT_ACCOUNT_ID, new BigDecimal("123.45")),
            is(dbTransactionWith(DEFAULT_ACCOUNT_ID, TransactionType.DEPOSIT, COMPLETED, 123.45, 0)));
    }

    @Test
    @DisplayName("when adding a deposit, should save a completed Transaction")
    public void whenAddingADeposit_shouldSaveACompletedTransaction() {
        var sut = new Ledger(accountRepository, transactionRepository, ledgerEntryRepository);

        sut.deposit(DEFAULT_ACCOUNT_ID, new BigDecimal("123.45"));

        ArgumentCaptor<Transaction> accountCaptor = captor();
        verify(transactionRepository).save(accountCaptor.capture());

        assertThat(
            accountCaptor.getValue(),
            is(dbTransactionWith(DEFAULT_ACCOUNT_ID, TransactionType.DEPOSIT, COMPLETED, 123.45, 0)));
    }

    @Test
    @DisplayName("when adding a deposit, should save deposit LedgerEntry")
    public void whenAddingADeposit_shouldSaveDepositLedgerEntry() {
        var sut = new Ledger(accountRepository, transactionRepository, ledgerEntryRepository);

        sut.deposit(DEFAULT_ACCOUNT_ID, new BigDecimal("123.45"));

        ArgumentCaptor<LedgerEntry> accountCaptor = captor();
        verify(ledgerEntryRepository).save(accountCaptor.capture());

        assertThat(
            accountCaptor.getValue(),
            is(dbLedgerEntryWith(LedgerEntryType.DEPOSIT, DEFAULT_ACCOUNT_ID, 123.45)));
    }

    @Test
    @DisplayName("when getting an account balance, should sum together all ledger items")
    public void whenGettingAnAccountBalance_shouldSumTogetherAllLedgerItems() {
        when(ledgerEntryRepository.findByAccountId(DEFAULT_ACCOUNT_ID)).thenReturn(List.of(
            ledgerEntryWithAmount(DEFAULT_ACCOUNT_ID, "123.45"),
            ledgerEntryWithAmount(DEFAULT_ACCOUNT_ID, "76.54"),
            ledgerEntryWithAmount(DEFAULT_ACCOUNT_ID, "-10")
        ));

        var sut = new Ledger(accountRepository, transactionRepository, ledgerEntryRepository);
        var balance = sut.getBalance(DEFAULT_ACCOUNT_ID);

        assertThat(balance, closeTo(new BigDecimal("189.99"), MONEY_EPSILON));
    }

    @Test
    @DisplayName("when getting a page of account balances, should sum together all ledger items for all given accounts")
    public void whenGettingAPageOdAccountBalances_shouldSumTogetherAllLedgerItemsForGivenAccounts() {
        var altAccountID = UUID.randomUUID();

        when(ledgerEntryRepository.findByAccountIdIn(List.of(DEFAULT_ACCOUNT_ID, altAccountID))).thenReturn(List.of(
            ledgerEntryWithAmount(DEFAULT_ACCOUNT_ID, "123.45"),
            ledgerEntryWithAmount(DEFAULT_ACCOUNT_ID, "76.54"),
            ledgerEntryWithAmount(altAccountID, "10"),
            ledgerEntryWithAmount(altAccountID, "-5")
        ));

        var sut = new Ledger(accountRepository, transactionRepository, ledgerEntryRepository);
        var balances = sut.getBalances(List.of(DEFAULT_ACCOUNT_ID, altAccountID), null, null);

        assertThat(balances, containsInAnyOrder(
            dtoAccountBalancePair(DEFAULT_ACCOUNT_ID, 199.99),
            dtoAccountBalancePair(altAccountID, 5.00)
        ));
    }

    @Test
    @DisplayName("when getting a page of account balances, should paginate it correctly")
    public void whenGettingAPageOfAccountBalances_shouldPaginateItCorrectly() {
        var accounts = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        var sut = new Ledger(accountRepository, transactionRepository, ledgerEntryRepository);
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

        var pageable = Pageable.ofSize(2).withPage(2);
        var page = new PageImpl<>(accountIds, pageable, 2000);
        when(accountRepository.fetchAccountIds(any())).thenReturn(page);

        var sut = new Ledger(accountRepository, transactionRepository, ledgerEntryRepository);
        var balances = sut.getBalances(null, 2, 2);

        assertThat(balances.getContent().stream().map(Pair::getFirst).toList(), contains(accountIds.toArray()));
    }

    @Test
    @DisplayName("given no account ids are given, when getting a page of account balances, should get accountIds from DB")
    public void givenEmptyAccountIdsAreGiven_whenGettingAPageOfAccountBalances_shouldPaginateItCorrectly() {
        var accountIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        var pageable = Pageable.ofSize(2).withPage(2);
        var page = new PageImpl<>(accountIds, pageable, 2000);
        when(accountRepository.fetchAccountIds(any())).thenReturn(page);

        var sut = new Ledger(accountRepository, transactionRepository, ledgerEntryRepository);
        var balances = sut.getBalances(List.of(), 2, 2);

        assertThat(balances.getContent().stream().map(Pair::getFirst).toList(), contains(accountIds.toArray()));
    }

    @Test
    @DisplayName("given a non existent Account, when adding a deposit, should throw an error")
    public void givenAnNonExistentAccount_whenAddingADeposit_shouldThrowAnError() {
        var sut = new Ledger(accountRepository, transactionRepository, ledgerEntryRepository);

        assertThrows(
            IllegalArgumentException.class, // TODO Change to an specific Exception later
            () -> sut.deposit(UUID.randomUUID(), new BigDecimal("123.45"))
        );
    }

    private static LedgerEntry ledgerEntryWithAmount(UUID accountId, String amount) {
        var account = Account.builder().id(accountId).build();
        return LedgerEntry.builder().account(account).amount(new BigDecimal(amount)).build();
    }
}
