package br.com.gabryel.waes.rdc.banking.service;

import br.com.gabryel.waes.rdc.banking.controller.dto.CreateAccountRequestDto;
import br.com.gabryel.waes.rdc.banking.model.Account;
import br.com.gabryel.waes.rdc.banking.model.AccountDocument;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AccountService {

    private final Map<UUID, Account> accounts = new HashMap<>();
    private final Map<UUID, List<AccountDocument>> accountDocuments = new HashMap<>();

    public Account saveAccount(CreateAccountRequestDto request) {
        var accountId = UUID.randomUUID();

        var mapped = new Account(accountId, request.name(), request.surname());
        accounts.put(accountId, mapped);
        return mapped;
    }

    public List<AccountDocument> saveDocuments(CreateAccountRequestDto request, UUID accountId) {
        var documents = request.documents().stream()
            .map(doc -> new AccountDocument(UUID.randomUUID(), accountId, doc.documentType(), doc.documentNumber()))
            .toList();

        accountDocuments.put(accountId, documents);
        return documents;
    }

    public Account getAccount(UUID accountId) {
        return accounts.get(accountId);
    }

    public List<AccountDocument> getAccountDocuments(UUID accountId) {
        return accountDocuments.get(accountId);
    }
}
