package com.onlydust.accounting.write.hexagon.gateways.repositories;

import com.onlydust.accounting.write.hexagon.models.Ledger;
import com.onlydust.accounting.write.hexagon.models.LedgerId;
import com.onlydust.accounting.write.hexagon.models.ProjectId;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface LedgerRepository {

    Optional<Ledger> byId(LedgerId ledgerId);

    BigDecimal getRemainingAmount(LedgerId ledgerId);

    Optional<Ledger> byProjectIdAndCurrency(ProjectId id, String currency);

    void save(Ledger ledger);
}
