package br.com.gabryel.waes.rdc.banking.controller;

import br.com.gabryel.waes.rdc.banking.controller.dto.AccountBalanceDto;
import br.com.gabryel.waes.rdc.banking.service.Ledger;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final Ledger ledger;

    // In-spec methods
    @GetMapping("/balances")
    public Page<AccountBalanceDto> getAccountBalance(
        @RequestParam("accountIds") List<UUID> accountIds,
        @RequestParam Integer pageSize,
        @RequestParam Integer pageNumber
    ) {
        return ledger.getBalances(accountIds, pageSize, pageNumber).map(accountToBalance ->
            new AccountBalanceDto(accountToBalance.getFirst(), accountToBalance.getSecond()));
    }
}
