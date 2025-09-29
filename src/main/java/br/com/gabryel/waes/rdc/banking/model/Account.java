package br.com.gabryel.waes.rdc.banking.model;

import java.util.UUID;

public record Account(UUID id, String name, String surname) {
}
