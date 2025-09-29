package br.com.gabryel.waes.rdc.banking.controller.dto;

import java.util.List;
import java.util.UUID;

public record AccountDto(UUID accountId, String name, String surname, List<DocumentDto> documents) {
}
