package com.onlydust.accounting.write.hexagon.gateways.repositories;

import com.onlydust.accounting.write.hexagon.models.Ledger;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface LedgerRepository {

    Optional<Ledger> byId(UUID ledgerId);

    BigDecimal getRemainingAmount(UUID ledgerId);

    Optional<Ledger> byProjectIdAndCurrency(UUID id, String currency);

    void save(Ledger ledger);
}
