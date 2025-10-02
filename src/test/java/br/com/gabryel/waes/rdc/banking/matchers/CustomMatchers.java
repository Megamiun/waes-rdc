package br.com.gabryel.waes.rdc.banking.matchers;

import br.com.gabryel.waes.rdc.banking.controller.dto.AccountDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.TransactionDto;
import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountDocument;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus;
import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.Matchers.*;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

@UtilityClass
public class CustomMatchers {

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
