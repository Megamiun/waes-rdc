package br.com.gabryel.waes.rdc.banking.matchers;

import br.com.gabryel.waes.rdc.banking.controller.dto.AccountDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.TransactionDto;
import br.com.gabryel.waes.rdc.banking.model.entity.LedgerEntry;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus;
import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static br.com.gabryel.waes.rdc.banking.matchers.CustomDbMatchers.MONEY_EPSILON;
import static org.hamcrest.Matchers.*;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

@UtilityClass
public class CustomDtoMatchers {

    public static <T> Matcher<ResponseEntity<T>> requestWith(HttpStatus status, T body) {
        return requestWith(status, equalTo(body));
    }

    public static <T> Matcher<ResponseEntity<T>> requestWith(HttpStatus status, Matcher<? super T> bodyMatcher) {
        return allOf(
            hasFeature("status code", ResponseEntity::getStatusCode, equalTo(HttpStatusCode.valueOf(status.value()))),
            hasFeature("body", ResponseEntity::getBody, bodyMatcher)
        );
    }

    @SafeVarargs
    public static Matcher<AccountDto> dtoAccountWith(String name, String surname, Matcher<? super DocumentDto>... documents) {
        return allOf(
            hasFeature(AccountDto::name, equalTo(name)),
            hasFeature(AccountDto::surname, equalTo(surname)),
            hasFeature(AccountDto::documents, contains(documents))
        );
    }

    public static Matcher<? super DocumentDto> dtoDocumentWith(DocumentType documentType, String documentNumber) {
        return allOf(
            hasFeature(DocumentDto::documentType, equalTo(documentType)),
            hasFeature(DocumentDto::documentNumber, equalTo(documentNumber))
        );
    }

    public static Matcher<? super TransactionDto> dtoTransactionWith(TransactionStatus status) {
        return hasFeature(TransactionDto::status, equalTo(status));
    }

    public static Matcher<? super Pair<UUID, BigDecimal>> dtoAccountBalancePair(UUID accountId, Double balance) {
        return allOf(
            hasFeature("accountId", Pair::getFirst, equalTo(accountId)),
            hasFeature("balance", Pair::getSecond, closeTo(BigDecimal.valueOf(balance), MONEY_EPSILON))
        );
    }
}
