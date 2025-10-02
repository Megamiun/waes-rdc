package br.com.gabryel.waes.rdc.banking.model.entity;

import br.com.gabryel.waes.rdc.banking.model.entity.enums.LedgerEntryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;

@Entity(name = "ledger_entry")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry implements IdHolder<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "account_id")
    @ManyToOne(fetch = EAGER)
    private Account account;

    @JoinColumn(name = "transaction_id")
    @ManyToOne(fetch = EAGER)
    private Transaction transaction;

    private BigDecimal amount;

    @Enumerated(STRING)
    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private LedgerEntryType type;

    private LocalDateTime createdAt;
}
