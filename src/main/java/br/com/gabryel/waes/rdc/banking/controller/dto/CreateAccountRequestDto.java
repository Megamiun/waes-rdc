package br.com.gabryel.waes.rdc.banking.controller.dto;

import java.util.List;

public record CreateAccountRequestDto(String name, String surname, List<DocumentDto> documents) {

}
