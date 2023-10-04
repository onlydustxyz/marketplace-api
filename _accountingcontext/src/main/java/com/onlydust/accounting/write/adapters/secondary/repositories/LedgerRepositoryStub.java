package com.onlydust.accounting.write.adapters.secondary.repositories;

import com.onlydust.accounting.write.hexagon.gateways.repositories.LedgerRepository;
import com.onlydust.accounting.write.hexagon.models.Ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LedgerRepositoryStub implements LedgerRepository {

    private final List<Ledger> ledgers = new ArrayList<>();

    @Override
    public Optional<Ledger> byId(UUID ledgerId) {
        return ledgers.stream().filter(ledger -> ledger.getId().equals(ledgerId)).findFirst();
    }

    public List<Ledger> ledgers() {
        return ledgers;
    }

    public void feedWith(Ledger eur) {
        ledgers.add(eur);
    }
}
