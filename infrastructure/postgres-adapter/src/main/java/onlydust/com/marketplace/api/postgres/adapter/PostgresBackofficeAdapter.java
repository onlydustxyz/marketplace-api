package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.EcosystemRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.project.domain.view.backoffice.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class PostgresBackofficeAdapter implements BackofficeStoragePort {

    private final GithubRepositoryLinkedToProjectRepository githubRepositoryLinkedToProjectRepository;
    private final BoSponsorRepository boSponsorRepository;
    private final ProjectLeadInvitationRepository projectLeadInvitationRepository;
    private final BoUserRepository boUserRepository;
    private final BoPaymentRepository boPaymentRepository;
    private final BoProjectRepository boProjectRepository;
    private final BoEcosystemRepository boEcosystemRepository;
    private final EcosystemRepository ecosystemRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectRepositoryView> findProjectRepositoryPage(Integer pageIndex, Integer pageSize,
                                                                 List<UUID> projectIds) {
        final var page =
                githubRepositoryLinkedToProjectRepository.findAllPublicForProjectsIds(PageRequest.of(pageIndex,
                        pageSize, Sort.by("owner", "name")), isNull(projectIds) ? List.of() : projectIds);
        return Page.<ProjectRepositoryView>builder()
                .content(page.getContent().stream().map(entity ->
                        ProjectRepositoryView.builder()
                                .projectId(entity.getId().getProjectId())
                                .id(entity.getId().getId())
                                .name(entity.getName())
                                .owner(entity.getOwner())
                                .technologies(entity.getTechnologies())
                                .build()
                ).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<EcosystemView> listEcosystems(int pageIndex, int pageSize, EcosystemView.Filters filters) {
        final var page = boEcosystemRepository.findAll(filters.getProjects(), filters.getEcosystems(),
                PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return Page.<EcosystemView>builder()
                .content(page.getContent().stream().map(BoEcosystemEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectLeadInvitationView> findProjectLeadInvitationPage(int pageIndex, int pageSize, List<UUID> ids,
                                                                         List<UUID> projectIds) {
        final var page = projectLeadInvitationRepository.findAllByIds(PageRequest.of(pageIndex, pageSize, Sort.by("id")),
                isNull(ids) ? List.of() : ids, isNull(projectIds) ? List.of() : projectIds);
        return Page.<ProjectLeadInvitationView>builder()
                .content(page.getContent().stream().map(entity ->
                        ProjectLeadInvitationView.builder()
                                .projectId(entity.getProjectId())
                                .id(entity.getId())
                                .githubUserId(entity.getGithubUserId())
                                .build()
                ).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<UserView> listUsers(int pageIndex, int pageSize, UserView.Filters filters) {
        final var page = boUserRepository.findAll(filters.getUsers(), PageRequest.of(pageIndex, pageSize,
                Sort.by(Sort.Direction.DESC, "created_at")));
        return Page.<UserView>builder()
                .content(page.getContent().stream().map(BoUserEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<PaymentView> listPayments(int pageIndex, int pageSize, PaymentView.Filters filters) {
        final var page = boPaymentRepository.findAll(filters.getProjects(), filters.getPayments(), PageRequest.of(pageIndex, pageSize,
                Sort.by(Sort.Direction.DESC, "requested_at")));
        return Page.<PaymentView>builder()
                .content(page.getContent().stream().map(BoPaymentEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<ProjectView> listProjects(int pageIndex, int pageSize, List<UUID> projectIds) {
        final var page = boProjectRepository.findAll(isNull(projectIds) ? List.of() : projectIds,
                PageRequest.of(pageIndex, pageSize));
        return Page.<ProjectView>builder()
                .content(page.getContent().stream().map(BoProjectEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public Ecosystem createEcosystem(Ecosystem ecosystem) {
        return ecosystemRepository.save(EcosystemEntity.fromDomain(ecosystem)).toDomain();
    }

    @Override
    @Transactional
    public void saveSponsor(Sponsor sponsor) {
        final var entity = boSponsorRepository.findById(sponsor.id())
                .map(e -> e.toBuilder()
                        .name(sponsor.name())
                        .url(sponsor.url())
                        .logoUrl(sponsor.logoUrl())
                        .build())
                .orElse(BoSponsorEntity.builder()
                        .id(sponsor.id())
                        .name(sponsor.name())
                        .url(sponsor.url())
                        .logoUrl(sponsor.logoUrl())
                        .build());
        boSponsorRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SponsorView> getSponsor(UUID sponsorId) {
        return boSponsorRepository.findById(sponsorId).map(BoSponsorEntity::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SponsorView> listSponsors(int pageIndex, int pageSize, SponsorView.Filters filters) {
        final var page = boSponsorRepository.findAll(filters.projects(), filters.sponsors(),
                PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return Page.<SponsorView>builder()
                .content(page.getContent().stream().map(BoSponsorEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }
}
