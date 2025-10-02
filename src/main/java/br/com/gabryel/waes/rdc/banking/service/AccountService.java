package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.request.CreateAccountRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountDocument;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType;
import br.com.gabryel.waes.rdc.banking.repository.AccountDocumentRepository;
import br.com.gabryel.waes.rdc.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType.BSN;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private final AccountDocumentRepository accountDocumentRepository;

    private final DocumentType primaryDocumentType;

    public AccountService(
        AccountRepository accountRepository,
        AccountDocumentRepository accountDocumentRepository,
        @Value("${app.document.primary-type}") DocumentType primaryDocumentType
    ) {
        this.accountRepository = accountRepository;
        this.accountDocumentRepository = accountDocumentRepository;
        this.primaryDocumentType = primaryDocumentType;
    }

    public Account saveAccount(CreateAccountRequestDto request) {
        var primaryDocument = request.documents().stream()
            .filter(doc -> doc.documentType() == primaryDocumentType)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Document of type " + primaryDocumentType + " not found"));

        if (accountDocumentRepository.existsByTypeAndNumber(primaryDocumentType, primaryDocument.documentNumber()))
            throw new IllegalStateException("Document of type " + primaryDocumentType + " with given number already exists");

        var account = accountRepository.save(map(request));
        saveDocuments(account, request.documents());

        return account;
    }

    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    public List<AccountDocument> getAccountDocuments(UUID accountId) {
        return accountDocumentRepository.findByAccountId(accountId);
    }

    private List<AccountDocument> saveDocuments(Account account, List<DocumentDto> documents) {
        var newDocuments = documents.stream()
            .map(doc -> map(account, doc))
            .toList();

        return accountDocumentRepository.saveAll(newDocuments);
    }

    private static Account map(CreateAccountRequestDto request) {
        return Account.builder()
            .name(request.name())
            .surname(request.surname())
            .build();
    }

    private static AccountDocument map(Account account, DocumentDto doc) {
        return AccountDocument.builder()
            .account(account)
            .type(doc.documentType())
            .number(doc.documentNumber())
            .build();
    }
}
