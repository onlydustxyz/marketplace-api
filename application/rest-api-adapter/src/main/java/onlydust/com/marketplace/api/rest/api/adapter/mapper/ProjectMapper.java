package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.ProjectRewardSettings;
import onlydust.com.marketplace.api.contract.model.ProjectVisibility;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.view.UserLinkView;

import java.util.Date;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.toZoneDateTime;

public interface ProjectMapper {
    static CreateProjectCommand mapCreateProjectCommandToDomain(CreateProjectRequest createProjectRequest,
                                                                UserId authenticatedUserId) {
        return CreateProjectCommand.builder()
                .name(createProjectRequest.getName())
                .shortDescription(createProjectRequest.getShortDescription())
                .longDescription(createProjectRequest.getLongDescription())
                .firstProjectLeaderId(authenticatedUserId)
                .githubUserIdsAsProjectLeadersToInvite(createProjectRequest.getInviteGithubUserIdsAsProjectLeads())
                .githubRepoIds(createProjectRequest.getGithubRepoIds())
                .isLookingForContributors(createProjectRequest.getIsLookingForContributors())
                .moreInfos(nonNull(createProjectRequest.getMoreInfos()) ? createProjectRequest.getMoreInfos().stream()
                        .map(moreInfo -> NamedLink.builder()
                                .url(moreInfo.getUrl()).value(moreInfo.getValue()).build()).toList() : null)
                .imageUrl(createProjectRequest.getLogoUrl())
                .ecosystemIds(createProjectRequest.getEcosystemIds())
                .categoryIds(createProjectRequest.getCategoryIds())
                .categorySuggestions(createProjectRequest.getCategorySuggestions())
                .contributorLabels(createProjectRequest.getContributorLabels().stream().map(ProjectContributorLabelRequest::getName).toList())
                .build();
    }

    static UpdateProjectCommand mapUpdateProjectCommandToDomain(ProjectId projectId,
                                                                UpdateProjectRequest updateProjectRequest) {
        return UpdateProjectCommand.builder()
                .id(projectId)
                .name(updateProjectRequest.getName())
                .shortDescription(updateProjectRequest.getShortDescription())
                .longDescription(updateProjectRequest.getLongDescription())
                .projectLeadersToKeep(updateProjectRequest.getProjectLeadsToKeep() == null ? null :
                        updateProjectRequest.getProjectLeadsToKeep().stream().map(UserId::of).toList())
                .githubUserIdsAsProjectLeadersToInvite(updateProjectRequest.getInviteGithubUserIdsAsProjectLeads())
                .rewardSettings(mapRewardSettingsToDomain(updateProjectRequest.getRewardSettings()))
                .githubRepoIds(updateProjectRequest.getGithubRepoIds())
                .isLookingForContributors(updateProjectRequest.getIsLookingForContributors())
                .moreInfos(isNull(updateProjectRequest.getMoreInfos()) ? null :
                        updateProjectRequest.getMoreInfos().stream()
                                .map(moreInfo -> NamedLink.builder()
                                        .url(moreInfo.getUrl()).value(moreInfo.getValue()).build()).toList())
                .imageUrl(updateProjectRequest.getLogoUrl())
                .ecosystemIds(updateProjectRequest.getEcosystemIds())
                .categoryIds(updateProjectRequest.getCategoryIds())
                .categorySuggestions(updateProjectRequest.getCategorySuggestions())
                .contributorLabels(updateProjectRequest.getContributorLabels().stream().map(l -> toProjectContributorLabel(projectId, l)).toList())
                .build();
    }

    static ProjectContributorLabel toProjectContributorLabel(ProjectId projectId, UpdateProjectRequestContributorLabelsInner label) {
        return label.getId() == null ? ProjectContributorLabel.of(projectId, label.getName()) :
                new ProjectContributorLabel(ProjectContributorLabel.Id.of(label.getId()), projectId, label.getName());
    }

    static ProjectRewardSettings mapRewardSettings(onlydust.com.marketplace.project.domain.model.ProjectRewardSettings rewardSettings) {
        final var projectRewardSettings = new ProjectRewardSettings();
        projectRewardSettings.setIgnorePullRequests(rewardSettings.getIgnorePullRequests());
        projectRewardSettings.setIgnoreIssues(rewardSettings.getIgnoreIssues());
        projectRewardSettings.setIgnoreCodeReviews(rewardSettings.getIgnoreCodeReviews());
        projectRewardSettings.setIgnoreContributionsBefore(toZoneDateTime(rewardSettings.getIgnoreContributionsBefore()));
        return projectRewardSettings;
    }

    static onlydust.com.marketplace.project.domain.model.ProjectRewardSettings mapRewardSettingsToDomain(ProjectRewardSettings rewardSettings) {
        if (rewardSettings == null) {
            return null;
        }

        return new onlydust.com.marketplace.project.domain.model.ProjectRewardSettings(
                rewardSettings.getIgnorePullRequests(),
                rewardSettings.getIgnoreIssues(),
                rewardSettings.getIgnoreCodeReviews(),
                isNull(rewardSettings.getIgnoreContributionsBefore()) ? null :
                        Date.from(rewardSettings.getIgnoreContributionsBefore().toInstant())
        );
    }

    static GithubUserResponse mapGithubUser(final UserLinkView userLinkView) {
        final var user = new GithubUserResponse();
        user.setGithubUserId(userLinkView.getGithubUserId());
        user.setAvatarUrl(userLinkView.getAvatarUrl());
        user.setLogin(userLinkView.getLogin());
        return user;
    }

    static ProjectVisibility mapProjectVisibility(onlydust.com.marketplace.project.domain.model.ProjectVisibility visibility) {
        switch (visibility) {
            case PUBLIC -> {
                return ProjectVisibility.PUBLIC;
            }
            case PRIVATE -> {
                return ProjectVisibility.PRIVATE;
            }
        }
        throw new IllegalArgumentException("Could not map project visibility");
    }

    static ProjectShortResponse mapShortProjectResponse(Project project) {
        return new ProjectShortResponse()
                .id(project.getId().value())
                .name(project.getName())
                .logoUrl(project.getLogoUrl())
                .slug(project.getSlug())
                .shortDescription(project.getShortDescription())
                .visibility(mapProjectVisibility(project.getVisibility()));
    }
}
