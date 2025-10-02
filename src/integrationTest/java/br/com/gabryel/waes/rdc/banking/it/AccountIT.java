package br.com.gabryel.waes.rdc.banking.it;

import br.com.gabryel.waes.rdc.banking.controller.dto.AccountDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.CreateAccountRequestDto;
import br.com.gabryel.waes.rdc.banking.it.extensions.TestContainersExtension;
import br.com.gabryel.waes.rdc.banking.it.services.ApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static br.com.gabryel.waes.rdc.banking.matchers.CustomMatchers.*;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType.BSN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(TestContainersExtension.class)
public class AccountIT {

    @Autowired
    private ApiClient client;

    @Test
    @DisplayName("when saving a valid Account, then should create account")
    public void whenSavingAnAccount_thenShouldCreateAccount() {
        var request = new CreateAccountRequestDto(
            "Name",
            "Surname",
            List.of(new DocumentDto(BSN, "123.456.789-0")));

        var result = client.put("/accounts", request, AccountDto.class);

        assertThat(result, requestWith(CREATED,
            dtoAccountWith(
                "Name",
                "Surname",
                dtoDocumentWith(BSN, "123.456.789-0"))
            )
        );
    }

    // TODO when saving an Account without BSN, then should not create account
    // TODO when saving an Account with duplicated BSN, then should not create account
}
