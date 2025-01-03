package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.EcosystemId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Ecosystem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EcosystemStoragePort {
    void save(@NonNull Ecosystem ecosystem);

    Optional<Ecosystem> get(@NonNull UUID ecosystemId);

    List<EcosystemId> getEcosystemLedIdsForUser(UserId userId);
}
