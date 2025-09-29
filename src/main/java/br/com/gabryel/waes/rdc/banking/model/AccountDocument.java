package br.com.gabryel.waes.rdc.banking.model;

import java.util.UUID;

public record AccountDocument(UUID id, UUID accountId, DocumentType documentType, String number) {
}
