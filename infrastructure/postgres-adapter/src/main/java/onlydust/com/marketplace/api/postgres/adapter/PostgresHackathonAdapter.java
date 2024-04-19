package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.HackathonDetailsViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HackathonEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.HackathonDetailsViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.HackathonRepository;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;

import java.util.Optional;

@AllArgsConstructor
public class PostgresHackathonAdapter implements HackathonStoragePort {
    private final HackathonRepository hackathonRepository;
    private final HackathonDetailsViewRepository hackathonDetailsViewRepository;

    @Override
    public void save(@NonNull Hackathon hackathon) {
        hackathonRepository.saveAndFlush(HackathonEntity.of(hackathon));
    }

    @Override
    public Optional<HackathonDetailsView> findById(@NonNull Hackathon.Id id) {
        return hackathonDetailsViewRepository.findById(id.value())
                .map(HackathonDetailsViewEntity::toDomain);
    }
}
