package com.onlydust.accounting.write.hexagon.gateways.repositories;

import com.onlydust.accounting.write.hexagon.models.Ledger;

import java.util.Optional;
import java.util.UUID;

public interface LedgerRepository {

    Optional<Ledger> byId(UUID ledgerId);

}
