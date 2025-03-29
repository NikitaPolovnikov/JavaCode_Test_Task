package org.example.service;

import org.example.dto.OperationRequest;
import org.example.dto.OperationType;
import org.example.dto.WalletResponse;
import org.example.exception.InsufficientFundsException;
import org.example.exception.WalletNotFoundException;
import org.example.model.Wallet;
import org.example.model.WalletTransaction;
import org.example.repository.WalletRepository;
import org.example.repository.WalletTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Transactional
    public WalletResponse processOperation(OperationRequest request) {
        // 1. Находим или создаем кошелек (в одной транзакции)
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setId(request.getWalletId());
                    newWallet.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(newWallet);
                });

        // 2. Перезагружаем с блокировкой
        Wallet lockedWallet = walletRepository.findByIdForUpdate(wallet.getId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));

        // 3. Вычисляем новый баланс
        BigDecimal newBalance = calculateNewBalance(lockedWallet, request);
        lockedWallet.setBalance(newBalance);

        // 4. Сохраняем транзакцию
        createTransaction(lockedWallet, request);

        return buildResponse(lockedWallet);
    }

    private BigDecimal calculateNewBalance(Wallet wallet, OperationRequest request) {
        if (request.getOperationType() == OperationType.DEPOSIT) {
            return wallet.getBalance().add(request.getAmount());
        } else {
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException(wallet.getId(), request.getAmount());
            }
            return wallet.getBalance().subtract(request.getAmount());
        }
    }

    private void createTransaction(Wallet wallet, OperationRequest request) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWallet(wallet); // Используем существующий managed-объект
        transaction.setAmount(request.getAmount());
        transaction.setOperationType(request.getOperationType().name());
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }
    public WalletResponse getWalletBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        return WalletResponse.builder()
                .walletId(wallet.getId())
                .balance(wallet.getBalance())
                .build();
    }

    private WalletResponse buildResponse(Wallet wallet) {
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }
}
