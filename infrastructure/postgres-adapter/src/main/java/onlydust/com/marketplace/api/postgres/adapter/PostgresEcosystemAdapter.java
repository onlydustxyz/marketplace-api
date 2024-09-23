package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.EcosystemLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.EcosystemRepository;
import onlydust.com.marketplace.kernel.model.EcosystemId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.port.output.EcosystemStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresEcosystemAdapter implements EcosystemStoragePort {

    private final EcosystemRepository ecosystemRepository;
    private final EcosystemLeadRepository ecosystemLeadRepository;

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

    @Override
    public List<EcosystemId> getEcosystemLedIdsForUser(UserId userId) {
        return ecosystemLeadRepository.findByUserId(userId.value())
                .stream()
                .map(EcosystemLeadEntity::getEcosystemId)
                .map(EcosystemId::of)
                .toList();
    }
}
