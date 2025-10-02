package br.com.gabryel.waes.rdc.banking.controller;

import br.com.gabryel.waes.rdc.banking.controller.dto.CardDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.request.CreateCardRequestDto;
import br.com.gabryel.waes.rdc.banking.model.entity.AccountCard;
import br.com.gabryel.waes.rdc.banking.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/accounts/{accountId}/cards")
@RequiredArgsConstructor
public class AccountCardsController {

    private final CardService cardService;

    // Created out of spec, for simplicity/testing
    @PutMapping("/{id}/cards")
    public ResponseEntity<CardDto> requestCard(@PathVariable("id") UUID id, @RequestBody CreateCardRequestDto request) {
        var card = cardService.requestCard(id, request);

        return ResponseEntity
            .created(URI.create("/accounts/" + id + "/cards/" + card.getId()))
            .body(mapToDto(card));
    }

    @GetMapping("/{id}/cards")
    public Page<CardDto> getCards(@PathVariable("id") UUID id) {
        return cardService.getCards(id).map(AccountCardsController::mapToDto);
    }

    private static CardDto mapToDto(AccountCard card) {
        return new CardDto(
            card.getId(),
            card.getType(),
            card.getPan(),
            card.getCvv(),
            card.getExpirationMonth(),
            card.getExpirationYear(),
            card.getLimit());
    }
}
