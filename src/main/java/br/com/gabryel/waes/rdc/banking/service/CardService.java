package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.request.CreateCardRequestDto;
import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountCard;
import br.com.gabryel.waes.rdc.banking.repository.AccountRepository;
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
    private static final String IIN = "55";
    private static final List<Character> numericCharacters =
        List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

    private final AccountRepository accountRepository;

    private final CardRepository cardRepository;

    private final BigDecimal defaultLimit;

    public CardService(
        AccountRepository accountRepository,
        CardRepository cardRepository,
        @Value("${app.card.limit}") BigDecimal defaultLimit
    ) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.defaultLimit = defaultLimit;
    }

    public AccountCard requestCard(UUID accountId, CreateCardRequestDto request) {
        var account = findAccount(accountId);

        if (cardRepository.existsByAccountIdAndType(accountId, request.type()))
            throw new IllegalStateException("Card with type " + request.type() + " already exists for account with id " + accountId);

        return cardRepository.save(createCard(account, request));
    }

    public Page<AccountCard> getCards(UUID accountId) {
        var account = findAccount(accountId);

        return cardRepository.findByAccountId(account.getId(), Pageable.unpaged());
    }

    private Account findAccount(UUID accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account with id " + accountId + " not found"));
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
            .limit(1000)
            .filter(pan -> !cardRepository.existsByPan(pan))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Failure finding unique credit card pan over 10000 tries. Will fail."));
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
