package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.request.CreateCardRequestDto;
import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountCard;
import br.com.gabryel.waes.rdc.banking.model.entity.CardType;
import br.com.gabryel.waes.rdc.banking.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static br.com.gabryel.waes.rdc.banking.matchers.CustomMocks.configureRepositoryMock;
import static br.com.gabryel.waes.rdc.banking.model.entity.CardType.DEBIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountCardServiceTest {
    private final static UUID DEFAULT_ACCOUNT_ID = UUID.randomUUID();

    @Mock(strictness = LENIENT)
    private AccountService accountService;

    @Mock(strictness = LENIENT)
    private CardRepository cardRepository;

    @BeforeEach
    void setUp() {
        when(accountService.findExistingAccount(DEFAULT_ACCOUNT_ID))
            .thenReturn(Account.builder().id(DEFAULT_ACCOUNT_ID).build());

        configureRepositoryMock(cardRepository);
    }

    @Test
    @DisplayName("when adding a card, should return card data given by request")
    public void whenAddingACard_shouldReturnCardData() {
        var sut = new CardService(accountService, cardRepository, new BigDecimal("2000"));

        assertThat(
            sut.requestCard(DEFAULT_ACCOUNT_ID, new CreateCardRequestDto(DEBIT, "Gabryel Monteiro")),
            allOf(
                hasFeature("accountId", card -> card.getAccount().getId(), equalTo(DEFAULT_ACCOUNT_ID)),
                hasFeature("holderName", AccountCard::getHolderName, equalTo("Gabryel Monteiro")),
                hasFeature("type", AccountCard::getType, equalTo(CardType.DEBIT))
            ));
    }

    @Test
    @DisplayName("when adding a card, should return expiration date 4 years in the future")
    public void whenAddingACard_shouldReturnReturnExpirationDate4YearsInTheFuture() {
        var sut = new CardService(accountService, cardRepository, new BigDecimal("2000"));

        // For consistency, I could have used a Clock, so there is no risk of creating turning a month mid-test run
        // For simplicity, I will not do so
        var currentDate = LocalDate.now();
        var expectedMonth = currentDate.getMonthValue();
        var expectedYear = currentDate.getYear() + 4;

        assertThat(
            sut.requestCard(DEFAULT_ACCOUNT_ID, new CreateCardRequestDto(DEBIT, "")),
            allOf(
                hasFeature("expiration month", AccountCard::getExpirationMonth, equalTo(expectedMonth)),
                hasFeature("expiration year", AccountCard::getExpirationYear, equalTo(expectedYear))
            ));
    }

    @CsvSource({"DEBIT,", "CREDIT,2000"})
    @DisplayName("when adding a card, should return correct limit")
    @ParameterizedTest(name = "when adding a {0} card, should return limit {1}")
    public void whenAddingACard_shouldReturnReturnExpirationDate4YearsInTheFuture(CardType type, BigDecimal limit) {
        var sut = new CardService(accountService, cardRepository, new BigDecimal("2000"));

        var result = sut.requestCard(DEFAULT_ACCOUNT_ID, new CreateCardRequestDto(type, ""));
        assertThat(result.getLimit(), equalTo(limit));
    }

    @Test
    @DisplayName("given account does not exist, when adding a card, should fail")
    public void givenAccountDoesNotExist_whenAddingACard_shouldFail() {
        var sut = new CardService(accountService, cardRepository, new BigDecimal("2000"));

        when(accountService.findExistingAccount(any())).thenThrow(new IllegalArgumentException());

        assertThrows(
            IllegalArgumentException.class,
            () -> sut.requestCard(UUID.randomUUID(), new CreateCardRequestDto(DEBIT, "")));
    }

    @Test
    @DisplayName("given card with type already exists, when adding a card, should fail")
    public void givenCardWithTypeAlreadyExists_whenAddingACard_shouldFail() {
        var sut = new CardService(accountService, cardRepository, new BigDecimal("2000"));

        when(cardRepository.existsByAccountIdAndType(DEFAULT_ACCOUNT_ID, DEBIT)).thenReturn(true);

        assertThrows(
            IllegalStateException.class,
            () -> sut.requestCard(DEFAULT_ACCOUNT_ID, new CreateCardRequestDto(DEBIT, "")));
    }
}
