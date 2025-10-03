package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.CreateAccountRequestDto;
import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountDocument;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType;
import br.com.gabryel.waes.rdc.banking.model.exceptions.MissingPrimaryDocument;
import br.com.gabryel.waes.rdc.banking.model.exceptions.NonExistentAccount;
import br.com.gabryel.waes.rdc.banking.model.exceptions.RepeatedPrimaryDocument;
import br.com.gabryel.waes.rdc.banking.repository.AccountDocumentRepository;
import br.com.gabryel.waes.rdc.banking.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

    @Transactional
    public Account saveAccount(CreateAccountRequestDto request) {
        var primaryDocument = request.documents().stream()
            .filter(doc -> doc.documentType() == primaryDocumentType)
            .findFirst()
            .orElseThrow(() -> new MissingPrimaryDocument(primaryDocumentType));

        if (accountDocumentRepository.existsByTypeAndNumber(primaryDocumentType, primaryDocument.documentNumber()))
            throw new RepeatedPrimaryDocument(primaryDocumentType);

        var account = accountRepository.save(map(request));
        saveDocuments(account, request.documents());

        return account;
    }

    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    public Page<Account> getAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    public Account findExistingAccount(UUID accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new NonExistentAccount(accountId));
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
