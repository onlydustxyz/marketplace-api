package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.api.domain.view.backoffice.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class PostgresBackofficeAdapter implements BackofficeStoragePort {

    private final GithubRepositoryLinkedToProjectRepository githubRepositoryLinkedToProjectRepository;
    private final ProjectBudgetRepository projectBudgetRepository;
    private final ProjectLeadInvitationRepository projectLeadInvitationRepository;
    private final BoPaymentRepository boPaymentRepository;
    private final BoProjectRepository boProjectRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectRepositoryView> findProjectRepositoryPage(Integer pageIndex, Integer pageSize,
                                                                 List<UUID> projectIds) {
        final var page =
                githubRepositoryLinkedToProjectRepository.findAllPublicForProjectsIds(PageRequest.of(pageIndex,
                        pageSize), isNull(projectIds) ? List.of() : projectIds);
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
        final var page = projectBudgetRepository.findAllByProjectIds(PageRequest.of(pageIndex, pageSize),
                isNull(projectIds) ? List.of() : projectIds);
        return Page.<ProjectBudgetView>builder()
                .content(page.getContent().stream().map(entity ->
                        ProjectBudgetView.builder()
                                .projectId(entity.getId().getProjectId())
                                .id(entity.getId().getId())
                                .currency(switch (entity.getCurrency()) {
                                    case eth -> Currency.Eth;
                                    case apt -> Currency.Apt;
                                    case stark -> Currency.Stark;
                                    case usd -> Currency.Usd;
                                    case op -> Currency.Op;
                                    case lords -> Currency.Lords;
                                })
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
    @Transactional(readOnly = true)
    public Page<ProjectLeadInvitationView> findProjectLeadInvitationPage(int pageIndex, int pageSize, List<UUID> ids) {
        final var page = projectLeadInvitationRepository.findAllByIds(PageRequest.of(pageIndex, pageSize),
                isNull(ids) ? List.of() : ids);
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
    public Page<PaymentView> listPayments(int pageIndex, int pageSize, List<UUID> projectIds) {
        final var page = boPaymentRepository.findAll(isNull(projectIds) ? List.of() : projectIds, PageRequest.of(pageIndex, pageSize));
        return Page.<PaymentView>builder()
                .content(page.getContent().stream().map(BoPaymentEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<ProjectView> listProjects(int pageIndex, int pageSize) {
        final var page = boProjectRepository.findAll(PageRequest.of(pageIndex, pageSize));
        return Page.<ProjectView>builder()
                .content(page.getContent().stream().map(BoProjectEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }
}
