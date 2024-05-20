package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.SponsorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoEcosystemQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoUserShortQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.EcosystemRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoEcosystemRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoUserShortViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorViewRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.project.domain.view.backoffice.BoSponsorView;
import onlydust.com.marketplace.project.domain.view.backoffice.EcosystemView;
import onlydust.com.marketplace.project.domain.view.backoffice.ProjectView;
import onlydust.com.marketplace.project.domain.view.backoffice.UserShortView;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresBackofficeAdapter implements BackofficeStoragePort {

    private final SponsorRepository sponsorRepository;
    private final SponsorViewRepository sponsorViewRepository;
    private final BoUserShortViewRepository boUserShortViewRepository;
    private final BoEcosystemRepository boEcosystemRepository;
    private final EcosystemRepository ecosystemRepository;
    private final ProjectRepository projectRepository;

    @Override
    public Page<EcosystemView> listEcosystems(int pageIndex, int pageSize, EcosystemView.Filters filters) {
        final var page = boEcosystemRepository.findAll(filters.getProjects(), filters.getEcosystems(),
                PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return Page.<EcosystemView>builder()
                .content(page.getContent().stream().map(BoEcosystemQueryEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<UserShortView> listUsers(int pageIndex, int pageSize, UserShortView.Filters filters) {
        final var page = boUserShortViewRepository.findAll(filters.loginLike().orElse(null), PageRequest.of(pageIndex, pageSize,
                Sort.by(Sort.Direction.DESC, "created_at")));
        return Page.<UserShortView>builder()
                .content(page.getContent().stream().map(BoUserShortQueryEntity::toDomain).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<ProjectView> searchProjects(int pageIndex, int pageSize, String search) {
        final var pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("name"));

        final var page = search != null
                ? projectRepository.findAllByNameContainingIgnoreCase(search, pageRequest)
                : projectRepository.findAll(pageRequest);

        return Page.<ProjectView>builder()
                .content(page.getContent().stream().map(ProjectEntity::toBoView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public Ecosystem createEcosystem(Ecosystem ecosystem) {
        return ecosystemRepository.saveAndFlush(EcosystemEntity.fromDomain(ecosystem)).toDomain();
    }

    @Override
    @Transactional
    public void saveSponsor(Sponsor sponsor) {
        final var entity = sponsorRepository.findById(sponsor.id())
                .map(e -> e.toBuilder()
                        .name(sponsor.name())
                        .url(sponsor.url())
                        .logoUrl(sponsor.logoUrl())
                        .build())
                .orElse(SponsorEntity.builder()
                        .id(sponsor.id())
                        .name(sponsor.name())
                        .url(sponsor.url())
                        .logoUrl(sponsor.logoUrl())
                        .build());
        sponsorRepository.saveAndFlush(entity);
    }

    @Override
    @Transactional
    public Optional<BoSponsorView> getSponsor(UUID sponsorId) {
        return sponsorViewRepository.findById(sponsorId).map(SponsorViewEntity::toBoView);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BoSponsorView> listSponsors(int pageIndex, int pageSize, BoSponsorView.Filters filters) {
        final var page = sponsorViewRepository.findAll(filters.projects(), filters.sponsors(),
                PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return Page.<BoSponsorView>builder()
                .content(page.getContent().stream().map(SponsorViewEntity::toBoView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }
}
