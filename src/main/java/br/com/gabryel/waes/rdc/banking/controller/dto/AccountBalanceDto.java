package br.com.gabryel.waes.rdc.banking.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceDto(UUID id, BigDecimal amount) {
}
