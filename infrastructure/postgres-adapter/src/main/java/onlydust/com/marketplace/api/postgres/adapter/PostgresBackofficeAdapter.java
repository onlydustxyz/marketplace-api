package onlydust.com.marketplace.api.postgres.adapter;

import static java.util.Objects.isNull;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.api.domain.view.backoffice.PaymentView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectBudgetView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectLeadInvitationView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectRepositoryView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectView;
import onlydust.com.marketplace.api.domain.view.backoffice.SponsorView;
import onlydust.com.marketplace.api.domain.view.backoffice.UserView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoSponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoPaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoSponsorRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.GithubRepositoryLinkedToProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.ProjectBudgetRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.ProjectLeadInvitationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class PostgresBackofficeAdapter implements BackofficeStoragePort {

  private final GithubRepositoryLinkedToProjectRepository githubRepositoryLinkedToProjectRepository;
  private final ProjectBudgetRepository projectBudgetRepository;
  private final BoSponsorRepository boSponsorRepository;
  private final ProjectLeadInvitationRepository projectLeadInvitationRepository;
  private final BoUserRepository boUserRepository;
  private final BoPaymentRepository boPaymentRepository;
  private final BoProjectRepository boProjectRepository;

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
  @Transactional(readOnly = true)
  public Page<ProjectBudgetView> findProjectBudgetPage(int pageIndex, int pageSize, List<UUID> projectIds) {
    final var page = projectBudgetRepository.findAllByProjectIds(PageRequest.of(pageIndex, pageSize, Sort.by("id")),
        isNull(projectIds) ? List.of() : projectIds);
    return Page.<ProjectBudgetView>builder()
        .content(page.getContent().stream().map(entity ->
            ProjectBudgetView.builder()
                .projectId(entity.getId().getProjectId())
                .id(entity.getId().getId())
                .currency(entity.getCurrency().toDomain())
                .initialAmount(entity.getInitialAmount())
                .remainingAmount(entity.getRemainingAmount())
                .spentAmount(entity.getSpentAmount())
                .initialAmountDollarsEquivalent(entity.getInitialAmountDollarsEquivalent())
                .remainingAmountDollarsEquivalent(entity.getRemainingAmountDollarsEquivalent())
                .spentAmountDollarsEquivalent(entity.getSpentAmountDollarsEquivalent())
                .build()
        ).toList())
        .totalItemNumber((int) page.getTotalElements())
        .totalPageNumber(page.getTotalPages())
        .build();
  }


  @Override
  public Page<SponsorView> listSponsors(int pageIndex, int pageSize, SponsorView.Filters filters) {
    final var page = boSponsorRepository.findAll(filters.getProjects(), filters.getSponsors(),
        PageRequest.of(pageIndex, pageSize, Sort.by("name")));
    return Page.<SponsorView>builder()
        .content(page.getContent().stream().map(BoSponsorEntity::toView).toList())
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
    final var page = boPaymentRepository.findAll(filters.getProjects(), filters.getPayments(),
        PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "requested_at")));
    return Page.<PaymentView>builder()
        .content(page.getContent().stream().map(BoPaymentEntity::toView).toList())
        .totalItemNumber((int) page.getTotalElements())
        .totalPageNumber(page.getTotalPages())
        .build();
  }

  @Override
  public Page<ProjectView> listProjects(int pageIndex, int pageSize, List<UUID> projectIds) {
    final var page = boProjectRepository.findAll(isNull(projectIds) ? List.of() : projectIds,
        PageRequest.of(pageIndex, pageSize, Sort.by("name")));
    return Page.<ProjectView>builder()
        .content(page.getContent().stream().map(BoProjectEntity::toView).toList())
        .totalItemNumber((int) page.getTotalElements())
        .totalPageNumber(page.getTotalPages())
        .build();
  }
}
