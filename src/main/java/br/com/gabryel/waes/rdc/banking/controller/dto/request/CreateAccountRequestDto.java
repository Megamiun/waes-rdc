package br.com.gabryel.waes.rdc.banking.controller.dto.request;

import br.com.gabryel.waes.rdc.banking.controller.dto.DocumentDto;

import java.util.List;

public record CreateAccountRequestDto(String name, String surname, List<DocumentDto> documents) {

}
