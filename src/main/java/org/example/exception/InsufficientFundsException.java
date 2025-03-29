package org.example.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends  RuntimeException{
    public InsufficientFundsException(UUID walletId, BigDecimal amount) {
        super("Not enough money in wallet " + walletId + " for amount " + amount);
    }
}
