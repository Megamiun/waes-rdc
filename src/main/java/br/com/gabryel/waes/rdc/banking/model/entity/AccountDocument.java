package br.com.gabryel.waes.rdc.banking.model.entity;

import br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;

@Entity(name = "account_document")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDocument implements IdHolder<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "account_id")
    @ManyToOne(fetch = EAGER)
    private Account account;

    @Enumerated(STRING) @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private DocumentType type;

    private String number;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
