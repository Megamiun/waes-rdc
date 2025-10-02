package br.com.gabryel.waes.rdc.banking.controller.dto;

import br.com.gabryel.waes.rdc.banking.model.entity.CardType;

import java.math.BigDecimal;
import java.util.UUID;

public record CardDto(
    UUID id,
    CardType type,
    String pan,
    String cvv,
    Integer expirationMonth,
    Integer expirationYear,
    BigDecimal limit) {
}
