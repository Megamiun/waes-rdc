package br.com.gabryel.waes.rdc.banking.matchers;

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
}
