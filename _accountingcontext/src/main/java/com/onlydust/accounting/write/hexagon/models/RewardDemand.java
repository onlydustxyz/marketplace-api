package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.AggregateRoot;

import java.math.BigDecimal;
import java.util.Objects;

public class RewardDemand extends AggregateRoot<RewardDemandId> {
    private final LedgerId ledgerId;
    private final BigDecimal amount;

    public RewardDemand(RewardDemandId id, LedgerId ledgerId, BigDecimal amount) {
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
