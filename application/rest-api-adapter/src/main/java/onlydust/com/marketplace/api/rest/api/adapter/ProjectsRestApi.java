package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.ProjectsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.ContributionUUID;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.ContributionType;
import onlydust.com.marketplace.project.domain.model.CreateAndCloseIssueCommand;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;
import onlydust.com.marketplace.project.domain.port.input.*;
import onlydust.com.marketplace.project.domain.view.*;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectBudgetMapper.mapProjectBudgetsViewToResponse;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.mapCreateProjectCommandToDomain;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.mapUpdateProjectCommandToDomain;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;
import static org.springframework.http.ResponseEntity.noContent;

@RestController
@Tags(@Tag(name = "Projects"))
@AllArgsConstructor
@Slf4j
@Profile("api")
public class ProjectsRestApi implements ProjectsApi {

    private final ProjectFacadePort projectFacadePort;
    private final ProjectRewardFacadePort projectRewardFacadePort;
    private final ProjectRewardFacadePort projectRewardFacadePortV2;
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final RewardFacadePort rewardFacadePort;
    private final ContributionFacadePort contributionsFacadePort;
    private final ProjectContributorLabelFacadePort projectContributorLabelFacadePort;

    @Override
    public ResponseEntity<CreateProjectResponse> createProject(CreateProjectRequest createProjectRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var projectIdentity =
                projectFacadePort.createProject(authenticatedUser.id(), mapCreateProjectCommandToDomain(createProjectRequest,
                        authenticatedUser.id()));

        final CreateProjectResponse createProjectResponse = new CreateProjectResponse();
        createProjectResponse.setProjectId(projectIdentity.getLeft().value());
        createProjectResponse.setProjectSlug(projectIdentity.getRight());
        return ResponseEntity.ok(createProjectResponse);
    }

    @Override
    public ResponseEntity<UpdateProjectResponse> updateProject(UUID projectId,
                                                               UpdateProjectRequest updateProjectRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var projectIdAndSlug = projectFacadePort.updateProject(authenticatedUser.id(),
                mapUpdateProjectCommandToDomain(ProjectId.of(projectId),
                        updateProjectRequest));
        return ResponseEntity.ok(new UpdateProjectResponse().projectId(projectIdAndSlug.getLeft().value()).projectSlug(projectIdAndSlug.getRight()));
    }

    @Override
    public ResponseEntity<UploadImageResponse> uploadProjectLogo(Resource image) {
        InputStream imageInputStream;
        try {
            imageInputStream = image.getInputStream();
        } catch (IOException e) {
            throw OnlyDustException.badRequest("Error while reading image data", e);
        }

        final URL imageUrl = projectFacadePort.saveLogoImage(imageInputStream);
        final UploadImageResponse response = new UploadImageResponse();
        response.url(imageUrl.toString());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProjectBudgetsResponse> getProjectBudgets(UUID projectId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final ProjectBudgetsView projectBudgetsView = projectRewardFacadePortV2.getBudgets(ProjectId.of(projectId),
                authenticatedUser.id());
        return ResponseEntity.ok(mapProjectBudgetsViewToResponse(projectBudgetsView));
    }

    @Override
    public ResponseEntity<CreateRewardResponse> createReward(UUID projectId, RewardRequest rewardRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var rewardId = rewardFacadePort.createReward(authenticatedUser.id(),
                RewardMapper.rewardRequestToDomain(rewardRequest, ProjectId.of(projectId)));
        final var response = new CreateRewardResponse();
        response.setId(rewardId.value());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> createRewards(UUID projectId, List<RewardRequest> rewardRequests) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final ProjectId typedProjectId = ProjectId.of(projectId);
        rewardFacadePort.createRewards(authenticatedUser.id(), rewardRequests.stream()
                .map(rewardRequest -> RewardMapper.rewardRequestToDomain(rewardRequest, typedProjectId))
                .toList());
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> cancelReward(UUID projectId, UUID rewardId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        rewardFacadePort.cancelReward(authenticatedUser.id(), ProjectId.of(projectId), RewardId.of(rewardId));
        return noContent().build();
    }

    @Override
    public ResponseEntity<RewardDetailsResponse> getProjectReward(UUID projectId, UUID rewardId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final RewardDetailsView rewardDetailsView = projectRewardFacadePort.getRewardByIdForProjectLead(ProjectId.of(projectId), rewardId,
                authenticatedUser.id());
        return ResponseEntity.ok(RewardMapper.projectRewardDetailsToResponse(rewardDetailsView, authenticatedUser));
    }

    @Override
    public ResponseEntity<RewardItemsPageResponse> getProjectRewardItemsPage(UUID projectId, UUID rewardId,
                                                                             Integer pageIndex, Integer pageSize) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = PaginationHelper.sanitizePageIndex(pageIndex);
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final Page<RewardItemView> page = projectRewardFacadePort.getRewardItemsPageByIdForProjectLead(ProjectId.of(projectId), rewardId,
                authenticatedUser.id(), sanitizedPageIndex, sanitizedPageSize);
        final RewardItemsPageResponse rewardItemsPageResponse = RewardMapper.pageToResponse(sanitizedPageIndex, page);
        return rewardItemsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(rewardItemsPageResponse) :
                ResponseEntity.ok(rewardItemsPageResponse);
    }

    @Override
    public ResponseEntity<RewardableItemsPageResponse> getProjectRewardableContributions(UUID projectId,
                                                                                         Long githubUserId,
                                                                                         Integer pageIndex,
                                                                                         Integer pageSize,
                                                                                         String search,
                                                                                         RewardType type,
                                                                                         ContributionStatus status,
                                                                                         Boolean includeIgnoredItems) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = PaginationHelper.sanitizePageIndex(pageIndex);
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final ContributionType contributionType = isNull(type) ? null : switch (type) {
            case ISSUE -> ContributionType.ISSUE;
            case PULL_REQUEST -> ContributionType.PULL_REQUEST;
            case CODE_REVIEW -> ContributionType.CODE_REVIEW;
        };
        final onlydust.com.marketplace.project.domain.model.ContributionStatus contributionStatus = isNull(status) ?
                null : switch (status) {
            case IN_PROGRESS -> onlydust.com.marketplace.project.domain.model.ContributionStatus.IN_PROGRESS;
            case COMPLETED -> onlydust.com.marketplace.project.domain.model.ContributionStatus.COMPLETED;
            case CANCELLED -> onlydust.com.marketplace.project.domain.model.ContributionStatus.CANCELLED;
        };
        final Page<RewardableItemView> rewardableItemsPage =
                projectFacadePort.getRewardableItemsPageByTypeForProjectLeadAndContributorId(ProjectId.of(projectId),
                        contributionType, contributionStatus,
                        authenticatedUser.id(), githubUserId, sanitizedPageIndex, sanitizedPageSize, search,
                        isNull(includeIgnoredItems) ? false : includeIgnoredItems);
        final RewardableItemsPageResponse rewardableItemsPageResponse =
                RewardableItemMapper.pageToResponse(sanitizedPageIndex,
                        rewardableItemsPage);
        return rewardableItemsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(rewardableItemsPageResponse) :
                ResponseEntity.ok(rewardableItemsPageResponse);
    }

    @Override
    public ResponseEntity<AllRewardableItemsResponse> getAllCompletedProjectRewardableContributions(UUID projectId,
                                                                                                    Long githubUserId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final List<RewardableItemView> rewardableItems =
                projectFacadePort.getAllCompletedRewardableItemsForProjectLeadAndContributorId(ProjectId.of(projectId),
                        authenticatedUser.id(), githubUserId);
        final AllRewardableItemsResponse response =
                RewardableItemMapper.listToResponse(rewardableItems);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ContributionDetailsResponse> getContribution(UUID projectId, String contributionId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var contribution = contributionsFacadePort.getContribution(ProjectId.of(projectId), contributionId, authenticatedUser);

        return ResponseEntity.ok(ContributionMapper.mapContributionDetails(contribution, authenticatedUser));
    }

    @Override
    public ResponseEntity<Void> updateIgnoredContributions(UUID projectId,
                                                           UpdateProjectIgnoredContributionsRequest updateProjectIgnoredContributionsRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (updateProjectIgnoredContributionsRequest.getContributionsToIgnore() != null &&
            !updateProjectIgnoredContributionsRequest.getContributionsToIgnore().isEmpty()) {
            contributionsFacadePort.ignoreContributions(ProjectId.of(projectId), authenticatedUser.id(),
                    updateProjectIgnoredContributionsRequest.getContributionsToIgnore());
        }
        if (updateProjectIgnoredContributionsRequest.getContributionsToUnignore() != null &&
            !updateProjectIgnoredContributionsRequest.getContributionsToUnignore().isEmpty()) {
            contributionsFacadePort.unignoreContributions(ProjectId.of(projectId), authenticatedUser.id(),
                    updateProjectIgnoredContributionsRequest.getContributionsToUnignore());
        }
        return noContent().build();
    }

    @Override
    public ResponseEntity<RewardableItemResponse> addRewardableOtherIssue(UUID projectId,
                                                                          AddOtherIssueRequest addOtherIssueRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final RewardableItemView issue = projectFacadePort.addRewardableIssue(ProjectId.of(projectId), authenticatedUser.id(),
                addOtherIssueRequest.getGithubIssueHtmlUrl());
        return ResponseEntity.ok(RewardableItemMapper.itemToResponse(issue));
    }

    @Override
    public ResponseEntity<RewardableItemResponse> addRewardableOtherPullRequest(UUID projectId,
                                                                                AddOtherPullRequestRequest addOtherPullRequestRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final RewardableItemView pullRequest = projectFacadePort.addRewardablePullRequest(ProjectId.of(projectId),
                authenticatedUser.id(),
                addOtherPullRequestRequest.getGithubPullRequestHtmlUrl());
        return ResponseEntity.ok(RewardableItemMapper.itemToResponse(pullRequest));
    }

    @Override
    public ResponseEntity<RewardableItemResponse> addRewardableOtherWork(UUID projectId,
                                                                         AddOtherWorkRequest addOtherWorkRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final RewardableItemView issue = projectFacadePort.createAndCloseIssueForProjectIdAndRepositoryId(
                CreateAndCloseIssueCommand.builder()
                        .projectId(ProjectId.of(projectId))
                        .projectLeadId(authenticatedUser.id())
                        .githubRepoId(addOtherWorkRequest.getGithubRepoId())
                        .title(addOtherWorkRequest.getTitle())
                        .description(addOtherWorkRequest.getDescription())
                        .build()
        );
        return ResponseEntity.ok(RewardableItemMapper.itemToResponse(issue));
    }

    @Override
    public ResponseEntity<ContributionPageResponse> getProjectContributions(UUID projectId,
                                                                            List<onlydust.com.marketplace.api.contract.model.ContributionType> types,
                                                                            List<ContributionStatus> statuses,
                                                                            List<Long> repositories,
                                                                            String fromDate,
                                                                            String toDate,
                                                                            List<Long> contributorIds,
                                                                            ProjectContributionSort sort,
                                                                            SortDirection direction,
                                                                            Integer pageIndex,
                                                                            Integer pageSize) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);

        final ContributionView.Filters filters = ContributionView.Filters.builder()
                .contributors(Optional.ofNullable(contributorIds).orElse(List.of()))
                .projects(List.of(ProjectId.of(projectId)))
                .repos(Optional.ofNullable(repositories).orElse(List.of()))
                .types(Optional.ofNullable(types).orElse(List.of()).stream().map(ContributionMapper::mapContributionType).toList())
                .statuses(Optional.ofNullable(statuses).orElse(List.of()).stream().map(ContributionMapper::mapContributionStatus).toList())
                .from(isNull(fromDate) ? null : DateMapper.parse(fromDate))
                .to(isNull(fromDate) ? null : DateMapper.parse(toDate))
                .build();

        final var contributions = projectFacadePort.contributions(
                ProjectId.of(projectId),
                authenticatedUser,
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

    @Override
    public ResponseEntity<ContributionPageResponse> getProjectStaledContributions(UUID projectId, Integer pageIndex,
                                                                                  Integer pageSize) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);

        final var contributions = projectFacadePort.staledContributions(
                ProjectId.of(projectId),
                authenticatedUser,
                sanitizedPageIndex,
                sanitizedPageSize);

        final var contributionPageResponse = ContributionMapper.mapContributionPageResponse(
                sanitizedPageIndex,
                contributions);

        return contributionPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(contributionPageResponse)
                : ResponseEntity.ok(contributionPageResponse);

    }

    @Override
    public ResponseEntity<ProjectChurnedContributorsPageResponse> getProjectChurnedContributors(UUID projectId,
                                                                                                Integer pageIndex,
                                                                                                Integer pageSize) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);

        final var contributors = projectFacadePort.churnedContributors(
                ProjectId.of(projectId),
                authenticatedUser,
                sanitizedPageIndex,
                sanitizedPageSize);

        final var response = ContributorMapper.mapProjectChurnedContributorsPageResponse(
                sanitizedPageIndex,
                contributors);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response)
                : ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<ProjectNewcomersPageResponse> getProjectNewcomers(UUID projectId, Integer pageIndex,
                                                                            Integer pageSize) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);

        final var contributors = projectFacadePort.newcomers(
                ProjectId.of(projectId),
                authenticatedUser,
                sanitizedPageIndex,
                sanitizedPageSize);

        final var response = ContributorMapper.mapProjectNewcomersPageResponse(
                sanitizedPageIndex,
                contributors);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response)
                : ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<ProjectContributorActivityPageResponse> getProjectMostActiveContributors(UUID projectId,
                                                                                                   Integer pageIndex,
                                                                                                   Integer pageSize) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);

        final var contributors = projectFacadePort.mostActives(
                ProjectId.of(projectId),
                authenticatedUser,
                sanitizedPageIndex,
                sanitizedPageSize);

        final var response = ContributorMapper.mapProjectContributorActivityPageResponse(
                sanitizedPageIndex,
                contributors);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response)
                : ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> hideContributor(UUID projectId, Long githubUserId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        projectFacadePort.hideContributorForProjectLead(ProjectId.of(projectId), authenticatedUser.id(), githubUserId);
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> showContributor(UUID projectId, Long githubUserId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        projectFacadePort.showContributorForProjectLead(ProjectId.of(projectId), authenticatedUser.id(), githubUserId);
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> unassignContribution(UUID projectId, String contributionId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        contributionsFacadePort.unassign(ProjectId.of(projectId), authenticatedUser.id(), contributionId);
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> unassignContributionV2(UUID projectId, UUID contributionUuid, Long contributorId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        contributionsFacadePort.unassign(ProjectId.of(projectId), authenticatedUser.id(), ContributionUUID.of(contributionUuid), contributorId);
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> ungrantFundsFromProject(UUID projectId, UngrantRequest ungrantRequest) {
        return ProjectsApi.super.ungrantFundsFromProject(projectId, ungrantRequest);
    }

    @Override
    public ResponseEntity<Void> updateContributorsLabels(UUID projectId, ContributorsLabelsRequest contributorsLabelsRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var labelsPerContributor = contributorsLabelsRequest.getContributorsLabels().stream()
                .collect(Collectors.toMap(ContributorLabelsRequest::getGithubUserId,
                        cl -> cl.getLabels().stream().map(ProjectContributorLabel.Id::of).toList()));

        projectContributorLabelFacadePort.updateLabelsOfContributors(authenticatedUser.id(), ProjectId.of(projectId), labelsPerContributor);
        return noContent().build();
    }
}
