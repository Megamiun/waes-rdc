package br.com.gabryel.waes.rdc.banking.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class Ledger {
    public Boolean deposit(UUID accountId, BigDecimal amount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
