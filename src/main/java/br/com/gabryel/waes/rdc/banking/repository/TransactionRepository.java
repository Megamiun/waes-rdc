package br.com.gabryel.waes.rdc.banking.repository;

import br.com.gabryel.waes.rdc.banking.model.entity.Transaction;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByOwnerIdInAndTypeIn(List<UUID> ownerId, List<TransactionType> types, Pageable page);

    Optional<Transaction> findByOwnerIdAndId(UUID ownerId, UUID transactionId);
}
