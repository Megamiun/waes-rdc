package br.com.gabryel.waes.rdc.banking.repository;

import br.com.gabryel.waes.rdc.banking.model.entity.TransactionTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionTransferRepository extends JpaRepository<TransactionTransfer, UUID> {
}
