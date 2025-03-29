package org.example.controller;

import org.example.dto.ErrorResponse;
import org.example.dto.OperationRequest;
import org.example.dto.WalletResponse;
import org.example.exception.InsufficientFundsException;
import org.example.exception.WalletNotFoundException;
import org.example.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<?> processWalletOperation(@RequestBody @Valid OperationRequest request) {
        try {
            WalletResponse response = walletService.processOperation(request);
            return ResponseEntity.ok(response);
        } catch (WalletNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), "Wallet not found"));
        } catch (InsufficientFundsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "Not enough money"));
        }
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<?> getWalletBalance(@PathVariable UUID walletId) {
        try {
            WalletResponse response = walletService.getWalletBalance(walletId);
            return ResponseEntity.ok(response);
        } catch (WalletNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), "Wallet not found"));
        }
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonException(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), "Invalid request"));
    }

}
