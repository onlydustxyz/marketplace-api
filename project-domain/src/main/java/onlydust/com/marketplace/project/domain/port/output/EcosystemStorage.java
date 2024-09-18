package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Ecosystem;

import java.util.Optional;
import java.util.UUID;

public interface EcosystemStorage {
    void save(@NonNull Ecosystem ecosystem);

    Optional<Ecosystem> get(@NonNull UUID ecosystemId);
}
