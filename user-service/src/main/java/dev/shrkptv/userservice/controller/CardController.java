package dev.shrkptv.userservice.controller;

import dev.shrkptv.userservice.database.entity.Card;
import dev.shrkptv.userservice.dto.CardCreateDTO;
import dev.shrkptv.userservice.dto.CardResponseDTO;
import dev.shrkptv.userservice.dto.CardUpdateDTO;
import dev.shrkptv.userservice.mapper.CardMapper;
import dev.shrkptv.userservice.services.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/card-info")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;
    private final CardMapper cardMapper;

    @PostMapping
    public ResponseEntity<CardResponseDTO> createCard(
            @Valid @RequestBody CardCreateDTO cardCreateDTO,
            @RequestParam Long userId)
    {
        Card card = cardService.createCard(cardMapper.toEntity(cardCreateDTO), userId);
        return ResponseEntity.status(201).body(cardMapper.toDto(card));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDTO> getCard(@PathVariable Long id)
    {
        return ResponseEntity.ok(cardMapper.toDto(cardService.getCard(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponseDTO> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody CardUpdateDTO cardUpdateDTO
            )
    {
        return ResponseEntity.ok(cardMapper.toDto(cardService.updateCard(id, cardUpdateDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id)
    {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
