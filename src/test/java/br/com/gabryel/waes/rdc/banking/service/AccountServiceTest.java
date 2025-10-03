package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.CreateAccountRequestDto;
import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountDocument;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType;
import br.com.gabryel.waes.rdc.banking.model.exceptions.MissingPrimaryDocument;
import br.com.gabryel.waes.rdc.banking.model.exceptions.NonExistentAccount;
import br.com.gabryel.waes.rdc.banking.model.exceptions.RepeatedPrimaryDocument;
import br.com.gabryel.waes.rdc.banking.repository.AccountDocumentRepository;
import br.com.gabryel.waes.rdc.banking.repository.AccountRepository;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static br.com.gabryel.waes.rdc.banking.matchers.CustomMocks.configureRepositoryMock;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType.BSN;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType.PASSPORT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.captor;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    private final static UUID DEFAULT_ACCOUNT_ID = UUID.randomUUID();
    private static final String TEST_NAME = "John";
    private static final String TEST_SURNAME = "Romero";
    private static final String BSN_NUMBER = "123.456.789-0";
    private static final String PASSPORT_NUMBER = "987.654.321-0";

    @Mock(strictness = LENIENT)
    private AccountRepository accountRepository;
    @Mock(strictness = LENIENT)
    private AccountDocumentRepository accountDocumentRepository;

    @BeforeEach
    void setUp() {
        configureRepositoryMock(accountRepository);
        configureRepositoryMock(accountDocumentRepository);
    }

    @Test
    @DisplayName("when saving an account, should save account details")
    public void whenSavingAnAccount_shouldReturnAccount() {
        var sut = new AccountService(accountRepository, accountDocumentRepository, BSN);
        var request = getDefaultCreateAccountRequest();

        assertThat(
            sut.saveAccount(request),
            is(dbAccountWith(TEST_NAME, TEST_SURNAME)));
    }

    @Test
    @DisplayName("when fetching an account, should return account details")
    public void whenFetchingAnAccount_shouldReturnAccountDetails() {
        var sut = new AccountService(accountRepository, accountDocumentRepository, BSN);
        accountRepository.save(Account.builder().id(DEFAULT_ACCOUNT_ID).name(TEST_NAME).surname(TEST_SURNAME).build());

        assertThat(
            sut.getAccount(DEFAULT_ACCOUNT_ID),
            is(dbAccountWith(TEST_NAME, TEST_SURNAME)));
    }

    @Test
    @DisplayName("given account doesn't exist, when fetching an existing account, should fail")
    public void givenAccountDoesntExist_whenFetchingAnAccount_shouldFail() {
        var sut = new AccountService(accountRepository, accountDocumentRepository, BSN);

        assertThrows(
            NonExistentAccount.class,
            () -> sut.findExistingAccount(DEFAULT_ACCOUNT_ID));
    }

    @Test
    @DisplayName("when fetching account documents, should return account details")
    public void whenFetchingAccountDocuments_shouldReturnAccountDocuments() {
        var sut = new AccountService(accountRepository, accountDocumentRepository, BSN);
        when(accountDocumentRepository.findByAccountId(DEFAULT_ACCOUNT_ID)).thenReturn(List.of(
            AccountDocument.builder().type(BSN).number(BSN_NUMBER).build(),
            AccountDocument.builder().type(PASSPORT).number(PASSPORT_NUMBER).build()));

        assertThat(
            sut.getAccountDocuments(DEFAULT_ACCOUNT_ID),
            contains(dbDocumentWith(BSN, BSN_NUMBER), dbDocumentWith(PASSPORT, PASSPORT_NUMBER)));
    }

    @Test
    @DisplayName("when saving an account, should save account details")
    public void whenSavingAnAccount_shouldSaveAccountDetails() {
        var sut = new AccountService(accountRepository, accountDocumentRepository, BSN);

        var request = getDefaultCreateAccountRequest();
        sut.saveAccount(request);

        ArgumentCaptor<Account> accountCaptor = captor();
        verify(accountRepository).save(accountCaptor.capture());

        assertThat(accountCaptor.getValue(), is(dbAccountWith(TEST_NAME, TEST_SURNAME)));
    }

    @Test
    @DisplayName("when saving an account, should save their documents")
    public void whenSavingAnAccount_shouldSaveTheirDocuments() {
        var sut = new AccountService(accountRepository, accountDocumentRepository, BSN);

        var request = getDefaultCreateAccountRequest();
        sut.saveAccount(request);

        ArgumentCaptor<List<AccountDocument>> documentCaptor = captor();
        verify(accountDocumentRepository).saveAll(documentCaptor.capture());

        assertThat(documentCaptor.getValue(), contains(
            dbDocumentWith(BSN, BSN_NUMBER),
            dbDocumentWith(PASSPORT, PASSPORT_NUMBER))
        );
    }

    @Test
    @DisplayName("given no BSN has been given, when saving an account, should fail with MissingPrimaryDocument")
    public void givenNoBsnHasBeenGiven_whenSavingAnAccount_shouldFail() {
        var sut = new AccountService(accountRepository, accountDocumentRepository, BSN);
        var request = new CreateAccountRequestDto(
            TEST_NAME,
            TEST_SURNAME,
            List.of(new DocumentDto(PASSPORT, PASSPORT_NUMBER))
        );

        assertThrows(
            MissingPrimaryDocument.class,
            () -> sut.saveAccount(request));
    }

    @Test
    @DisplayName("given BSN already exists, when saving an account, should fail with RepeatedPrimaryDocument")
    public void givenBsnAlreadyExists_whenSavingAnAccount_shouldFail() {
        var sut = new AccountService(accountRepository, accountDocumentRepository, BSN);
        var request = new CreateAccountRequestDto(
            TEST_NAME,
            TEST_SURNAME,
            List.of(new DocumentDto(BSN, BSN_NUMBER))
        );

        when(accountDocumentRepository.existsByTypeAndNumber(BSN, BSN_NUMBER)).thenReturn(true);

        assertThrows(
            RepeatedPrimaryDocument.class,
            () -> sut.saveAccount(request));
    }

    private static CreateAccountRequestDto getDefaultCreateAccountRequest() {
        return new CreateAccountRequestDto(
            TEST_NAME,
            TEST_SURNAME,
            List.of(
                new DocumentDto(BSN, BSN_NUMBER),
                new DocumentDto(PASSPORT, PASSPORT_NUMBER)
            )
        );
    }

    public static Matcher<Account> dbAccountWith(String name, String surname) {
        return allOf(
            hasFeature("name", Account::getName, equalTo(name)),
            hasFeature("surname", Account::getSurname, equalTo(surname))
        );
    }

    public static Matcher<AccountDocument> dbDocumentWith(DocumentType documentType, String documentNumber) {
        return allOf(
            hasFeature("type", AccountDocument::getType, equalTo(documentType)),
            hasFeature("number", AccountDocument::getNumber, equalTo(documentNumber))
        );
    }
}
