package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.AggregateRoot;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@ToString
public class Allocation extends AggregateRoot {
    private final UUID ledgerId;
    private final BigDecimal amount;

    public Allocation(UUID id, UUID ledgerId, BigDecimal amount) {
        super(id);
        this.ledgerId = ledgerId;
        this.amount = amount;
    }

}
