package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HackathonEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HackathonRegistrationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.HackathonRegistrationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.HackathonRepository;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresHackathonAdapter implements HackathonStoragePort {
    private final HackathonRepository hackathonRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;

    @Override
    @Transactional
    public void save(@NonNull Hackathon hackathon) {
        hackathonRepository.saveAndFlush(HackathonEntity.of(hackathon));
    }

    @Override
    @Transactional
    public void saveStatus(Hackathon.@NonNull Id hackathonId, Hackathon.@NonNull Status status) {
        final var hackathon = hackathonRepository.findById(hackathonId.value())
                .orElseThrow(() -> new IllegalArgumentException("Hackathon %s not found".formatted(hackathonId)));
        hackathon.setStatus(status);
        hackathonRepository.saveAndFlush(hackathon);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Hackathon.Id id) {
        return hackathonRepository.existsById(id.value());
    }

    @Override
    public boolean hasUserRegistered(Hackathon.Id hackathonId) {
        return hackathonRegistrationRepository.existsByHackathonId(hackathonId.value());
    }

    @Override
    @Transactional
    public void delete(Hackathon.Id hackathonId) {
        hackathonRepository.deleteById(hackathonId.value());
    }

    @Override
    @Transactional
    public void registerUser(UUID userId, Hackathon.Id hackathonId) {
        hackathonRegistrationRepository.saveAndFlush(new HackathonRegistrationEntity(hackathonId.value(), userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Hackathon> findUpcomingHackathonByIssueId(GithubIssue.@NonNull Id issueId) {
        return hackathonRepository.findUpcomingHackathonByIssueId(issueId.value()).map(HackathonEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Hackathon> findById(Hackathon.@NonNull Id id) {
        return hackathonRepository.findById(id.value()).map(HackathonEntity::toDomain);
    }
}
