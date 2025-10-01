package br.com.gabryel.waes.rdc.banking.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@Entity(name = "account_document")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDocument implements EntityWithId<UUID> {
    @Id
    private UUID id;
    private UUID accountId;
    @Enumerated(STRING)
    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private DocumentType type;
    private String number;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
