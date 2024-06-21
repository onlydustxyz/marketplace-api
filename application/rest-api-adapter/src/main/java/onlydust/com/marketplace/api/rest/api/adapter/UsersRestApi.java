package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.UsersApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.ContributionMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.ContributorSearchResponseMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.SortDirectionMapper;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.project.domain.view.ContributionView;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;


@RestController
@Tags(@Tag(name = "Users"))
@AllArgsConstructor
@Profile("api")
public class UsersRestApi implements UsersApi {

    private final static int DEFAULT_MAX_INTERNAL_CONTRIBUTOR_COUNT_TO_TRIGGER_EXTERNAL_SEARCH = 5;
    private final static int DEFAULT_MAX_INTERNAL_CONTRIBUTOR_COUNT_TO_RETURN = 50;

    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final ContributorFacadePort contributorFacadePort;

    @Override
    public ResponseEntity<ContributorSearchResponse> searchContributors(UUID projectId,
                                                                        List<Long> repoIds,
                                                                        String login,
                                                                        Integer maxInternalContributorCountToTriggerExternalSearch,
                                                                        Integer maxInternalContributorCountToReturn,
                                                                        Boolean externalSearchOnly) {
        final var contributors = contributorFacadePort.searchContributors(
                projectId,
                repoIds != null ? new HashSet<>(repoIds) : null,
                login,
                maxInternalContributorCountToTriggerExternalSearch != null ?
                        maxInternalContributorCountToTriggerExternalSearch :
                        DEFAULT_MAX_INTERNAL_CONTRIBUTOR_COUNT_TO_TRIGGER_EXTERNAL_SEARCH,
                maxInternalContributorCountToReturn != null ? maxInternalContributorCountToReturn :
                        DEFAULT_MAX_INTERNAL_CONTRIBUTOR_COUNT_TO_RETURN,
                Boolean.TRUE.equals(externalSearchOnly));
        return ResponseEntity.ok(ContributorSearchResponseMapper.of(contributors.getLeft(), contributors.getRight()));
    }


    @Override
    public ResponseEntity<ContributionPageResponse> getUserContributions(Long githubUserId,
                                                                         List<ContributionType> types,
                                                                         List<ContributionStatus> statuses,
                                                                         List<UUID> projects,
                                                                         List<Long> repositories,
                                                                         List<UUID> languages,
                                                                         List<UUID> ecosystems,
                                                                         Boolean includePrivateProjects,
                                                                         String fromDate,
                                                                         String toDate,
                                                                         ContributionSort sort,
                                                                         String direction,
                                                                         Integer page,
                                                                         Integer pageSize) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(page);

        final var caller = authenticatedAppUserService.tryGetAuthenticatedUser();
        if (caller.isEmpty()) {
            includePrivateProjects = false;
        }

        final var filters = ContributionView.Filters.builder()
                .contributors(List.of(githubUserId))
                .projects(Optional.ofNullable(projects).orElse(List.of()))
                .repos(Optional.ofNullable(repositories).orElse(List.of()))
                .types(Optional.ofNullable(types).orElse(List.of()).stream().map(ContributionMapper::mapContributionType).toList())
                .statuses(Optional.ofNullable(statuses).orElse(List.of()).stream().map(ContributionMapper::mapContributionStatus).toList())
                .languages(Optional.ofNullable(languages).orElse(List.of()))
                .ecosystems(Optional.ofNullable(ecosystems).orElse(List.of()))
                .includePrivateProjects(includePrivateProjects)
                .from(isNull(fromDate) ? null : DateMapper.parse(fromDate))
                .to(isNull(toDate) ? null : DateMapper.parse(toDate))
                .build();

        final var contributions = contributorFacadePort.contributions(
                caller.map(User::getGithubUserId),
                filters,
                ContributionMapper.mapSort(sort),
                SortDirectionMapper.requestToDomain(direction),
                sanitizedPageIndex,
                sanitizedPageSize);

        final var contributionPageResponse = ContributionMapper.mapContributionPageResponse(
                sanitizedPageIndex,
                contributions);

        return contributionPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(contributionPageResponse)
                : ResponseEntity.ok(contributionPageResponse);

    }
}
