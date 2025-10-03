package br.com.gabryel.waes.rdc.banking.controller;

import br.com.gabryel.waes.rdc.banking.controller.dto.CardDto;
import br.com.gabryel.waes.rdc.banking.controller.dto.PageDto;
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
    @PutMapping
    public ResponseEntity<CardDto> requestCard(@PathVariable("accountId") UUID accountId, @RequestBody CreateCardRequestDto request) {
        var card = cardService.requestCard(accountId, request);

        return ResponseEntity
            .created(URI.create("/accounts/" + accountId + "/cards/" + card.getId()))
            .body(mapToDto(card));
    }

    @GetMapping
    public PageDto<CardDto> getCards(@PathVariable("accountId") UUID accountId) {
        return PageDto.of(cardService.getCards(accountId)).map(AccountCardsController::mapToDto);
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
