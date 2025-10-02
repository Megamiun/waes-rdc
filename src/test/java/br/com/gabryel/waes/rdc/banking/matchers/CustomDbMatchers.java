package br.com.gabryel.waes.rdc.banking.matchers;

import br.com.gabryel.waes.rdc.banking.model.entity.Account;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountDocument;
import br.com.gabryel.waes.rdc.banking.model.entity.LedgerEntry;
import br.com.gabryel.waes.rdc.banking.model.entity.Transaction;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.LedgerEntryType;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionStatus;
import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionType;
import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

@UtilityClass
public class CustomDbMatchers {

    public static BigDecimal MONEY_EPSILON = BigDecimal.valueOf(0.01);

    public static Matcher<Account> dbAccountWith(String name, String surname) {
        return allOf(
            hasFeature("name", Account::getName, equalTo(name)),
            hasFeature("surname", Account::getSurname, equalTo(surname))
        );
    }

    public static Matcher<AccountDocument> dbDocumentWith(DocumentType documentType, String documentNumber) {
        return allOf(
            hasFeature("type", AccountDocument::getType, equalTo(documentType)),
            hasFeature("number", AccountDocument::getNumber, equalTo(documentNumber))
        );
    }

    public static Matcher<Transaction> dbTransactionWith(UUID accountId, TransactionType type, TransactionStatus status, double amount, double feeAmount) {
        return allOf(
            hasFeature("ownerId", transaction -> transaction.getOwner().getId(), equalTo(accountId)),
            hasFeature("amount", Transaction::getAmount, closeTo(BigDecimal.valueOf(amount), MONEY_EPSILON)),
            hasFeature("feeAmount", Transaction::getFeeAmount, closeTo(BigDecimal.valueOf(feeAmount), MONEY_EPSILON)),
            hasFeature("type", Transaction::getType, equalTo(type)),
            hasFeature("status", Transaction::getStatus, equalTo(status))
        );
    }

    public static Matcher<LedgerEntry> dbLedgerEntryWith(LedgerEntryType type, UUID accountId, double amount) {
        return allOf(
            hasFeature("accountId", entry -> entry.getAccount().getId(), equalTo(accountId)),
            hasFeature("amount", LedgerEntry::getAmount, closeTo(BigDecimal.valueOf(amount), MONEY_EPSILON)),
            hasFeature("type", LedgerEntry::getType, equalTo(type))
        );
    }
}
