package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.AggregateRoot;
import com.onlydust.shared.write.hexagon.models.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class RewardDemand extends AggregateRoot {
    private final UUID ledgerId;
    private final BigDecimal amount;

    public RewardDemand(UUID id, UUID ledgerId, BigDecimal amount) {
        super(id);
        this.ledgerId = ledgerId;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RewardDemand that = (RewardDemand) o;
        return Objects.equals(ledgerId, that.ledgerId) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ledgerId, amount);
    }
}
