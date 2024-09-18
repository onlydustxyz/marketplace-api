package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.EcosystemRepository;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.port.output.EcosystemStorage;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresEcosystemAdapter implements EcosystemStorage {

    private final EcosystemRepository ecosystemRepository;

    @Override
    @Transactional
    public void save(@NonNull Ecosystem ecosystem) {
        ecosystemRepository.findById(ecosystem.id())
                .ifPresentOrElse(e -> e.updateWith(ecosystem), () -> ecosystemRepository.save(EcosystemEntity.of(ecosystem)));
    }

    @Override
    public Optional<Ecosystem> get(@NonNull UUID ecosystemId) {
        return ecosystemRepository.findById(ecosystemId)
                .map(EcosystemEntity::toDomain);
    }
}
