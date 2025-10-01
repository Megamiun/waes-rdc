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
        var accountId = UUID.randomUUID();
        var account = accountRepository.save(new Account(accountId, request.name(), request.surname()));

        saveDocuments(accountId, request.documents());

        return account;
    }

    public List<AccountDocument> saveDocuments(UUID accountId, List<DocumentDto> documents) {
        var newDocuments = documents.stream()
            .map(doc -> new AccountDocument(UUID.randomUUID(), accountId, doc.documentType(), doc.documentNumber()))
            .toList();

        return accountDocumentRepository.saveAll(newDocuments);
    }

    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    public List<AccountDocument> getAccountDocuments(UUID accountId) {
        return accountDocumentRepository.findByAccountId(accountId);
    }
}
