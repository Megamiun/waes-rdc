package br.com.gabryel.waes.rdc.banking.repository;

import br.com.gabryel.waes.rdc.banking.model.entity.AccountCard;
import br.com.gabryel.waes.rdc.banking.model.entity.CardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<AccountCard, UUID> {
    Page<AccountCard> findByAccountId(UUID accountId, Pageable pageable);
    boolean existsByAccountIdAndType(UUID accountId, CardType type);
    boolean existsByPan(String pan);
}

