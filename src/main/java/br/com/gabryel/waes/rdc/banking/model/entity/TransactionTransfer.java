package br.com.gabryel.waes.rdc.banking.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

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

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "id")
    private Transaction transaction;

    @JoinColumn(name = "receiver_id")
    @ManyToOne(fetch = EAGER)
    private Account receiver;
}
