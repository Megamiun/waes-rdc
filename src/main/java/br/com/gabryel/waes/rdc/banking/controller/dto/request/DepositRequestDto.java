package br.com.gabryel.waes.rdc.banking.controller.dto.request;

import java.math.BigDecimal;

public record DepositRequestDto(BigDecimal amount) {
}
