package com.onlydust.accounting.write.adapters.secondary.repositories;

import com.onlydust.accounting.write.hexagon.gateways.repositories.LedgerRepository;
import com.onlydust.accounting.write.hexagon.models.Ledger;

import java.math.BigDecimal;
import java.util.*;

public class LedgerRepositoryStub implements LedgerRepository {

    private final List<Ledger> ledgers = new ArrayList<>();
    private final Map<UUID, BigDecimal> remainingAmounts = new HashMap<>();

    @Override
    public Optional<Ledger> byId(UUID ledgerId) {
        return ledgers.stream().filter(ledger -> ledger.getId().equals(ledgerId)).findFirst();
    }

    @Override
    public BigDecimal getRemainingAmount(UUID ledgerId) {
        return remainingAmounts.get(ledgerId);
    }

    public List<Ledger> ledgers() {
        return ledgers;
    }

    public void feedWith(Ledger ledger) {
        ledgers.add(ledger);
    }

    public void setRemainingAmount(UUID ledgerId, BigDecimal amount) {
        remainingAmounts.put(ledgerId, amount);
    }


}
