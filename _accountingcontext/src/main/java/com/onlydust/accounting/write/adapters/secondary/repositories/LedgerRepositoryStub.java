package com.onlydust.accounting.write.adapters.secondary.repositories;

import com.onlydust.accounting.write.hexagon.gateways.repositories.LedgerRepository;
import com.onlydust.accounting.write.hexagon.models.Ledger;
import com.onlydust.accounting.write.hexagon.models.LedgerId;
import com.onlydust.accounting.write.hexagon.models.ProjectId;

import java.math.BigDecimal;
import java.util.*;

public class LedgerRepositoryStub implements LedgerRepository {

    private final List<Ledger> ledgers = new ArrayList<>();
    private final Map<LedgerId, BigDecimal> remainingAmounts = new HashMap<>();

    @Override
    public Optional<Ledger> byId(LedgerId ledgerId) {
        return ledgers.stream().filter(ledger -> ledger.getId().equals(ledgerId)).findFirst();
    }

    @Override
    public BigDecimal getRemainingAmount(LedgerId ledgerId) {
        return remainingAmounts.get(ledgerId);
    }

    @Override
    public Optional<Ledger> byProjectIdAndCurrency(ProjectId projectId, String currency) {
        return ledgers.stream().filter(ledger -> ledger.getProjectId().equals(projectId) && ledger.getCurrency().equals(currency)).findFirst();
    }

    @Override
    public void save(Ledger ledger) {
        ledgers.add(ledger);
    }

    public List<Ledger> ledgers() {
        return ledgers;
    }

    public void setRemainingAmount(LedgerId ledgerId, BigDecimal amount) {
        remainingAmounts.put(ledgerId, amount);
    }

}
