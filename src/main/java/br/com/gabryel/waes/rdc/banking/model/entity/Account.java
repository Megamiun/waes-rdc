package br.com.gabryel.waes.rdc.banking.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "account")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements EntityWithId<UUID> {
    @Id
    private UUID id;
    private String name;
    private String surname;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
