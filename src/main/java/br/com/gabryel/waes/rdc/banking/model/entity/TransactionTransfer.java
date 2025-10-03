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

@Entity(name = "transaction_transfer")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTransfer implements IdHolder<UUID> {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Transaction transaction;

    @JoinColumn(name = "receiver_id")
    @ManyToOne(fetch = EAGER)
    private Account receiver;
}
