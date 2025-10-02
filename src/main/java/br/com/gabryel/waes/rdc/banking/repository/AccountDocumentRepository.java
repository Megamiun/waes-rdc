package br.com.gabryel.waes.rdc.banking.repository;

import br.com.gabryel.waes.rdc.banking.model.entity.AccountDocument;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountDocumentRepository extends JpaRepository<AccountDocument, UUID> {

    List<AccountDocument> findByAccountId(UUID accountId);

    boolean existsByTypeAndNumber(DocumentType type, String number);
}
