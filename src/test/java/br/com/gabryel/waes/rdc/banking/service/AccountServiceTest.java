package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.CreateAccountRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import br.com.gabryel.waes.rdc.banking.model.Account;
import br.com.gabryel.waes.rdc.banking.model.AccountDocument;
import br.com.gabryel.waes.rdc.banking.repository.AccountDocumentRepository;
import br.com.gabryel.waes.rdc.banking.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static br.com.gabryel.waes.rdc.banking.matchers.CustomMatchers.dbAccountWith;
import static br.com.gabryel.waes.rdc.banking.matchers.CustomMatchers.dbDocumentWith;
import static br.com.gabryel.waes.rdc.banking.matchers.CustomMocks.configureRepositoryMock;
import static br.com.gabryel.waes.rdc.banking.model.DocumentType.BSN;
import static br.com.gabryel.waes.rdc.banking.model.DocumentType.PASSPORT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentCaptor.captor;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
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
    public void whenSavingAnAccount_shouldSaveAccountDetails() {
        var sut = new AccountService(accountRepository, accountDocumentRepository);

        var request = getDefaultCreateAccountRequest();
        sut.saveAccount(request);

        ArgumentCaptor<Account> accountCaptor = captor();
        verify(accountRepository).save(accountCaptor.capture());

        assertThat(accountCaptor.getValue(), is(dbAccountWith(TEST_NAME, TEST_SURNAME)));
    }

    @Test
    @DisplayName("when saving an account, should save their documents")
    public void whenSavingAnAccount_shouldSaveTheirDocuments() {
        var sut = new AccountService(accountRepository, accountDocumentRepository);

        var request = getDefaultCreateAccountRequest();
        sut.saveAccount(request);

        ArgumentCaptor<List<AccountDocument>> documentCaptor = captor();
        verify(accountDocumentRepository).saveAll(documentCaptor.capture());

        assertThat(documentCaptor.getValue(), contains(
            dbDocumentWith(BSN, BSN_NUMBER),
            dbDocumentWith(PASSPORT, PASSPORT_NUMBER))
        );
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
}
