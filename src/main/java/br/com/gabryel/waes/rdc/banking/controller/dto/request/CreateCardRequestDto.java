package br.com.gabryel.waes.rdc.banking.controller.dto.request;

import br.com.gabryel.waes.rdc.banking.model.entity.CardType;

public record CreateCardRequestDto(CardType type, String holderName) {
}
