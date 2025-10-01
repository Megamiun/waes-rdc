package br.com.gabryel.waes.rdc.banking.it.matchers;

import br.com.gabryel.waes.rdc.banking.controller.dto.AccountDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import br.com.gabryel.waes.rdc.banking.model.DocumentType;
import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

@UtilityClass
public class CustomMatchers {

    public static Matcher<ResponseEntity<AccountDto>> requestWith(HttpStatus status, Matcher<AccountDto> bodyMatcher) {
        return allOf(
            hasFeature("status code", ResponseEntity::getStatusCode, equalTo(HttpStatusCode.valueOf(status.value()))),
            hasFeature("body", ResponseEntity::getBody, bodyMatcher)
        );
    }

    @SafeVarargs
    public static Matcher<AccountDto> accountWith(String name, String surname, Matcher<? super DocumentDto>... documents) {
        return allOf(
            hasFeature(AccountDto::name, equalTo(name)),
            hasFeature(AccountDto::surname, equalTo(surname)),
            hasFeature(AccountDto::documents, contains(documents))
        );
    }

    public static Matcher<? super DocumentDto> documentWith(DocumentType documentType, String documentNumber) {
        return allOf(
            hasFeature(DocumentDto::documentType, equalTo(documentType)),
            hasFeature(DocumentDto::documentNumber, equalTo(documentNumber))
        );
    }
}
