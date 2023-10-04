package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.AggregateRoot;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@ToString
public class Allocation extends AggregateRoot<AllocationId> {
    private final LedgerId ledgerId;
    private final BigDecimal amount;

    public Allocation(AllocationId id, LedgerId ledgerId, BigDecimal amount) {
        super(id);
        this.ledgerId = ledgerId;
        this.amount = amount;
    }

}
