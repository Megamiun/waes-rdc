package br.com.gabryel.waes.rdc.banking.model.entity;

public interface IdHolder<T> {
    T getId();
    void setId(T id);
}
