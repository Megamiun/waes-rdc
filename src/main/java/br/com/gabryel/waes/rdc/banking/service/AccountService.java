package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.CreateAccountRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountDocument;
import br.com.gabryel.waes.rdc.banking.repository.AccountDocumentRepository;
import br.com.gabryel.waes.rdc.banking.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private final AccountDocumentRepository accountDocumentRepository;

    public AccountService(AccountRepository accountRepository, AccountDocumentRepository accountDocumentRepository) {
        this.accountRepository = accountRepository;
        this.accountDocumentRepository = accountDocumentRepository;
    }

    public Account saveAccount(CreateAccountRequestDto request) {
        var account = accountRepository.save(map(request));

        saveDocuments(account.getId(), request.documents());

        return account;
    }

    public List<AccountDocument> saveDocuments(UUID accountId, List<DocumentDto> documents) {
        var newDocuments = documents.stream()
            .map(doc -> map(accountId, doc))
            .toList();

        return accountDocumentRepository.saveAll(newDocuments);
    }

    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    public List<AccountDocument> getAccountDocuments(UUID accountId) {
        return accountDocumentRepository.findByAccountId(accountId);
    }

    private static Account map(CreateAccountRequestDto request) {
        return Account.builder()
            .id(UUID.randomUUID())
            .name(request.name())
            .surname(request.surname())
            .build();
    }

    private static AccountDocument map(UUID accountId, DocumentDto doc) {
        return AccountDocument.builder()
            .id(UUID.randomUUID())
            .accountId(accountId)
            .type(doc.documentType())
            .number(doc.documentNumber())
            .build();
    }
}
