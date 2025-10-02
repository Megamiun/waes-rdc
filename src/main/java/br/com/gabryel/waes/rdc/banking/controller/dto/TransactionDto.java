package br.com.gabryel.waes.rdc.banking.controller.dto;

import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus;

import java.util.UUID;

public record TransactionDto(UUID id, TransactionStatus status) {
}
