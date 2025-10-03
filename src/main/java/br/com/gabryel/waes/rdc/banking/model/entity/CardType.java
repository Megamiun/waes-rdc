package br.com.gabryel.waes.rdc.banking.model.entity;

import br.com.gabryel.waes.rdc.banking.model.entity.enums.TransactionMethod;

public enum CardType {
    CREDIT,
    DEBIT;

    public TransactionMethod asTransactionMethod() {
        switch (this) {
            case CREDIT -> {
                return TransactionMethod.CREDIT;
            }
            case DEBIT -> {
                return TransactionMethod.DEBIT;
            }
            default -> throw new IllegalStateException("CardType has no transaction method: " + this);
        }
    }
}
