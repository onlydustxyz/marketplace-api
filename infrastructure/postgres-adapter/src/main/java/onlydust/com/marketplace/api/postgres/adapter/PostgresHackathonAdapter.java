package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.HackathonDetailsViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.HackathonShortViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HackathonEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.HackathonDetailsViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.HackathonRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.HackathonShortViewRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;
import onlydust.com.marketplace.project.domain.view.HackathonShortView;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
public class PostgresHackathonAdapter implements HackathonStoragePort {
    private final HackathonRepository hackathonRepository;
    private final HackathonDetailsViewRepository hackathonDetailsViewRepository;
    private final HackathonShortViewRepository hackathonShortViewRepository;

    @Override
    public void save(@NonNull Hackathon hackathon) {
        hackathonRepository.saveAndFlush(HackathonEntity.of(hackathon));
    }

    @Override
    public Optional<HackathonDetailsView> findById(@NonNull Hackathon.Id id) {
        return hackathonDetailsViewRepository.findById(id.value())
                .map(HackathonDetailsViewEntity::toDomain);
    }

    @Override
    public boolean exists(Hackathon.Id id) {
        return hackathonRepository.existsById(id.value());
    }

    @Override
    public Page<HackathonShortView> findByStatuses(int pageIndex, int pageSize, Set<Hackathon.Status> statuses) {
        final var page = hackathonShortViewRepository.findByStatusIn(statuses, PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.ASC, "startDate")));
        return Page.<HackathonShortView>builder()
                .content(page.getContent().stream().map(HackathonShortViewEntity::toDomain).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public void delete(Hackathon.Id hackathonId) {
        hackathonRepository.deleteById(hackathonId.value());
    }
}
