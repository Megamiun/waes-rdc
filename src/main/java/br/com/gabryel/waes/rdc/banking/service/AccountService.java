package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.request.CreateAccountRequestDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;
import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountDocument;
import br.com.gabryel.waes.rdc.banking.repository.AccountDocumentRepository;
import br.com.gabryel.waes.rdc.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final AccountDocumentRepository accountDocumentRepository;

    public Account saveAccount(CreateAccountRequestDto request) {
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
