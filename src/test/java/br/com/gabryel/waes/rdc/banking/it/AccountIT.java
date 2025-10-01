package br.com.gabryel.waes.rdc.banking.it;

import br.com.gabryel.waes.rdc.banking.controller.dto.AccountDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.CreateAccountRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static br.com.gabryel.waes.rdc.banking.it.matchers.CustomMatchers.*;
import static br.com.gabryel.waes.rdc.banking.model.DocumentType.BSN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AccountIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("when saving a valid Account, then should create account")
    public void whenSavingAnAccount_thenShouldCreateAccount() {
        var request = new CreateAccountRequestDto(
            "Name",
            "Surname",
            List.of(new DocumentDto(BSN, "123.456.789-0")));

        var result = restTemplate.exchange("/accounts", PUT, new HttpEntity<>(request), AccountDto.class);

        assertThat(result, requestWith(CREATED,
            accountWith(
                "Name",
                "Surname",
                documentWith(BSN, "123.456.789-0"))
            )
        );
    }

    // TODO when saving an Account without BSN, then should not create account
    // TODO when saving an Account with duplicated BSN, then should not create account
}
