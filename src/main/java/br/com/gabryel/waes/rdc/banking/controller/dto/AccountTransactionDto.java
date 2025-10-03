package br.com.gabryel.waes.rdc.banking.controller.dto;

import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountTransactionDto(UUID ownerId, UUID id, TransactionStatus status, BigDecimal amount, BigDecimal fee) {
}
