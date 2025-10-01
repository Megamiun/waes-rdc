package br.com.gabryel.waes.rdc.banking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@Entity(name = "account_document")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDocument {
    @Id
    private UUID id;
    private UUID accountId;
    @Enumerated(STRING)
    private DocumentType type;
    private String number;
}
