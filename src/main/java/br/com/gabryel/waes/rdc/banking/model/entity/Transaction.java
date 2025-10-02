package br.com.gabryel.waes.rdc.banking.model.entity;

import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionMethod;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;

@Entity(name = "transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements EntityWithId<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "owner_id")
    @ManyToOne(fetch = EAGER)
    private Account owner;

    private BigDecimal amount;
    private BigDecimal feeAmount;

    @Enumerated(STRING)
    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private TransactionType type;

    @Enumerated(STRING)
    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private TransactionMethod method;

    @Enumerated(STRING)
    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private TransactionStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
