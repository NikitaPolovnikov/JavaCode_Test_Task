package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OperationRequest {
    @NotNull(message = "walletId is required")
    @JsonProperty("walletId")
    private UUID walletId;

    @NotNull(message = "operationType is required")
    private OperationType operationType;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;
}

