package br.com.gabryel.waes.rdc.banking.it;

import br.com.gabryel.waes.rdc.banking.controller.dto.AccountDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.BalanceDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.TransactionDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.CreateAccountRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.DepositRequestDto;
import br.com.gabryel.waes.rdc.banking.it.extensions.TestContainersExtension;
import br.com.gabryel.waes.rdc.banking.it.services.ApiClient;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static br.com.gabryel.waes.rdc.banking.matchers.CustomMatchers.requestWith;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType.BSN;
import static br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus.COMPLETED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(TestContainersExtension.class)
public class TransactionIT {

    @Autowired
    private ApiClient client;

    @Test
    @DisplayName("when making a deposit, should add value to user account")
    public void whenMakingADeposit_thenShouldAddValueToUserAccount() {
        var accountId = setupDefaultAccount();
        var depositValue = new BigDecimal("123.45");

        var result = client.put(
            "/accounts/" + accountId + "/transactions/deposits",
            new DepositRequestDto(depositValue),
            TransactionDto.class
        );

        assertThat(result, requestWith(CREATED, dtoTransactionWith(COMPLETED)));

        var balanceResult = client.get("/accounts/" + accountId + "/balance", BalanceDto.class);
        assertThat(balanceResult, requestWith(OK, new BalanceDto(depositValue)));
    }

    private UUID setupDefaultAccount() {
        var request = new CreateAccountRequestDto(
            "Name",
            "Surname",
            List.of(new DocumentDto(BSN, "123.456.789-0")));

        return client.put("/accounts", request, AccountDto.class)
            .getBody()
            .accountId();
    }

    public static Matcher<? super TransactionDto> dtoTransactionWith(TransactionStatus status) {
        return hasFeature(TransactionDto::status, equalTo(status));
    }
}
