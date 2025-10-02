package br.com.gabryel.waes.rdc.banking.controller.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestDto(UUID receiverAccountId, UUID cardId, BigDecimal amount) {
}
