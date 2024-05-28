package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.HackathonDetailsQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.HackathonShortViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoUserShortQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HackathonEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HackathonRegistrationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.HackathonDetailsViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.HackathonRegistrationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.HackathonRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.HackathonShortViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoUserShortViewRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;
import onlydust.com.marketplace.project.domain.view.HackathonShortView;
import onlydust.com.marketplace.project.domain.view.backoffice.UserShortView;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class PostgresHackathonAdapter implements HackathonStoragePort {
    private final HackathonRepository hackathonRepository;
    private final HackathonDetailsViewRepository hackathonDetailsViewRepository;
    private final HackathonShortViewRepository hackathonShortViewRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;
    private final BoUserShortViewRepository boUserShortViewRepository;

    @Override
    public void save(@NonNull Hackathon hackathon) {
        hackathonRepository.saveAndFlush(HackathonEntity.of(hackathon));
    }

    @Override
    public void saveStatus(Hackathon.@NonNull Id hackathonId, Hackathon.@NonNull Status status) {
        final var hackathon = hackathonRepository.findById(hackathonId.value())
                .orElseThrow(() -> new IllegalArgumentException("Hackathon %s not found".formatted(hackathonId)));
        hackathon.setStatus(status);
        hackathonRepository.saveAndFlush(hackathon);
    }

    @Override
    public Optional<HackathonDetailsView> findById(@NonNull Hackathon.Id id) {
        return hackathonDetailsViewRepository.findById(id.value())
                .map(HackathonDetailsQueryEntity::toDomain);
    }

    @Override
    public Optional<HackathonDetailsView> findBySlug(String hackathonSlug) {
        return hackathonDetailsViewRepository.findBySlug(hackathonSlug)
                .map(HackathonDetailsQueryEntity::toDomain);
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

    @Override
    public void registerUser(UUID userId, Hackathon.Id hackathonId) {
        hackathonRegistrationRepository.saveAndFlush(new HackathonRegistrationEntity(hackathonId.value(), userId));
    }

    @Override
    public boolean isRegisteredToHackathon(UUID userId, Hackathon.Id hackathonId) {
        return hackathonRegistrationRepository.existsById(new HackathonRegistrationEntity.PrimaryKey(hackathonId.value(), userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserShortView> listRegisteredUsers(Hackathon.Id hackahtonId, int pageIndex, int pageSize, UserShortView.Filters filters) {
        final var page = boUserShortViewRepository.findAllRegisteredOnHackathon(filters.loginLike().orElse(null), hackahtonId.value(),
                PageRequest.of(pageIndex, pageSize,
                JpaSort.unsafe(Sort.Direction.DESC, "tech_created_at")));
        return Page.<UserShortView>builder()
                .content(page.getContent().stream().map(BoUserShortQueryEntity::toDomain).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }
}
