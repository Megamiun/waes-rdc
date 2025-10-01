package br.com.gabryel.waes.rdc.banking.model;

import jakarta.persistence.Id;

public interface EntityWithId<T> {
    @Id
    T getId();
}
