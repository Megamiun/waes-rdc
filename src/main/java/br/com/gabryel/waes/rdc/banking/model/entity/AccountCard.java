package br.com.gabryel.waes.rdc.banking.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.FetchType.EAGER;

@Entity(name = "account_card")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCard implements IdHolder<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "account_id")
    @ManyToOne(fetch = EAGER)
    private Account account;

    private String holderName;

    private CardType type;
    private String pan;
    private String cvv;

    private Integer expirationMonth;
    private Integer expirationYear;

    @Column(name = "card_limit")
    private BigDecimal limit;

    private LocalDateTime createdAt;
}
