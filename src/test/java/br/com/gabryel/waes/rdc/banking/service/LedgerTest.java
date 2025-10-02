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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static br.com.gabryel.waes.rdc.banking.matchers.CustomDbMatchers.*;
import static br.com.gabryel.waes.rdc.banking.matchers.CustomMocks.configureRepositoryMock;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus.COMPLETED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.captor;
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
            ledgerEntryWithAmount("123.45"),
            ledgerEntryWithAmount("76.54"),
            ledgerEntryWithAmount("-10")
        ));

        var sut = new Ledger(accountRepository, transactionRepository, ledgerEntryRepository);
        var balance = sut.getBalance(DEFAULT_ACCOUNT_ID);

        assertThat(balance, closeTo(new BigDecimal("190"), MONEY_EPSILON));
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

    private static LedgerEntry ledgerEntryWithAmount(String amount) {
        return LedgerEntry.builder().amount(new BigDecimal(amount)).build();
    }
}
