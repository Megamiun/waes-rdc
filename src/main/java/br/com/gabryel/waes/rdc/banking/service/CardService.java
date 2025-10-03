package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.request.CreateCardRequestDto;
import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountCard;
import br.com.gabryel.waes.rdc.banking.model.exceptions.CannotCreateUniqueCardPan;
import br.com.gabryel.waes.rdc.banking.model.exceptions.NonExistentCardForAccount;
import br.com.gabryel.waes.rdc.banking.model.exceptions.RepeatedCardTypeForAccount;
import br.com.gabryel.waes.rdc.banking.repository.CardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static br.com.gabryel.waes.rdc.banking.model.entity.CardType.CREDIT;

@Service
public class CardService {
    private final int retries = 1000;
    private static final String IIN = "55";
    private static final List<Character> numericCharacters =
        List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

    private final AccountService accountService;

    private final CardRepository cardRepository;

    private final BigDecimal defaultLimit;

    public CardService(
        AccountService accountService,
        CardRepository cardRepository,
        @Value("${app.card.limit}") BigDecimal defaultLimit
    ) {
        this.accountService = accountService;
        this.cardRepository = cardRepository;
        this.defaultLimit = defaultLimit;
    }

    public AccountCard requestCard(UUID accountId, CreateCardRequestDto request) {
        var account = accountService.findExistingAccount(accountId);

        if (cardRepository.existsByAccountIdAndType(accountId, request.type()))
            throw new RepeatedCardTypeForAccount(accountId, request.type());

        return cardRepository.save(createCard(account, request));
    }

    public Page<AccountCard> getCards(UUID accountId) {
        var account = accountService.findExistingAccount(accountId);

        return cardRepository.findByAccountId(account.getId(), Pageable.unpaged());
    }
    public AccountCard getExistentCard(UUID accountId, UUID cardId) {
        var card = cardRepository.findByAccountIdAndId(accountId, cardId);
        if (card == null) throw new NonExistentCardForAccount(accountId, cardId);

        return card;
    }

    private AccountCard createCard(Account account, CreateCardRequestDto request) {
        var currentDate = LocalDate.now();

        var cvv = generateRandomNumbers("", 3);
        var pan = generateUniqueCreditCardNumber();
        var cardBuilder = AccountCard.builder()
            .account(account)
            .holderName(request.holderName())
            .type(request.type())
            .expirationYear(currentDate.getYear() + 4)
            .expirationMonth(currentDate.getMonthValue())
            .cvv(cvv)
            .pan(pan);

        if (request.type() == CREDIT)
            return cardBuilder.limit(defaultLimit).build();

        return cardBuilder.build();
    }

    private String generateUniqueCreditCardNumber() {
        return Stream.iterate(generateRandomNumbers(IIN, 19), prev -> generateRandomNumbers(IIN, 19))
            .limit(retries)
            .filter(pan -> !cardRepository.existsByPan(pan))
            .findFirst()
            .orElseThrow(() -> new CannotCreateUniqueCardPan(retries));
    }

    private static String generateRandomNumbers(String prefix, int size) {
        var random = new Random();
        var builder = new StringBuilder(prefix);

        while (builder.length() < size) {
            builder.append(getRandomDigit(random));
        }

        return builder.toString();
    }

    private static Character getRandomDigit(Random random) {
        return numericCharacters.get(random.nextInt(numericCharacters.size()));
    }
}
