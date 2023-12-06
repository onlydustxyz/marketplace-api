package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectBudgetView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectRepositoryView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.GithubRepositoryLinkedToProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.ProjectBudgetRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class PostgresBackofficeAdapter implements BackofficeStoragePort {

    private final GithubRepositoryLinkedToProjectRepository githubRepositoryLinkedToProjectRepository;
    private final ProjectBudgetRepository projectBudgetRepository;

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
}
