package br.com.gabryel.waes.rdc.banking.controller.dto;

import br.com.gabryel.waes.rdc.banking.model.DocumentType;

public record DocumentDto(DocumentType documentType, String documentNumber) {
}
