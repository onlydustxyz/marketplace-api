package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;
import onlydust.com.marketplace.api.domain.view.ProjectOrganizationView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLeadViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.GithubUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public interface ProjectMapper {

    static ProjectDetailsView mapToProjectDetailsView(ProjectViewEntity projectEntity,
                                                      List<GithubUserViewEntity> topContributors,
                                                      Integer contributorCount,
                                                      List<ProjectLeadViewEntity> leaders,
                                                      List<SponsorEntity> sponsors,
                                                      final BigDecimal remainingUsdBudget) {

        final var organizationEntities = new HashMap<Long, GithubAccountEntity>();
        projectEntity.getRepos().forEach(repo -> organizationEntities.put(repo.getOwner().getId(), repo.getOwner()));
        final var repoIdsIncludedInProject =
                projectEntity.getRepos().stream().map(GithubRepoEntity::getId).collect(Collectors.toSet());

        final var organizations = organizationEntities.values().stream().map(entity -> ProjectOrganizationView.builder()
                .id(entity.getId())
                .login(entity.getLogin())
                .avatarUrl(entity.getAvatarUrl())
                .htmlUrl(entity.getHtmlUrl())
                .name(entity.getName())
                .installationId(isNull(entity.getInstallation()) ? null : entity.getInstallation().getId())
                .repos(entity.getRepos().stream()
                        .map(repo -> RepoMapper.mapToDomain(repo,
                                repoIdsIncludedInProject.contains(repo.getId()),
                                entity.getInstallation() != null &&
                                entity.getInstallation().getAuthorizedRepos().stream()
                                        .anyMatch(installedRepo -> installedRepo.getId().getRepoId().equals(repo.getId())))
                        )
                        .collect(Collectors.toSet()))
                .build()).toList();

        final var project = ProjectDetailsView.builder()
                .id(projectEntity.getId())
                .hiring(projectEntity.getHiring())
                .logoUrl(projectEntity.getLogoUrl())
                .longDescription(projectEntity.getLongDescription())
                .shortDescription(projectEntity.getShortDescription())
                .slug(projectEntity.getKey())
                .name(projectEntity.getName())
                .createdAt(Date.from(projectEntity.getCreatedAt()))
                .moreInfoUrl(projectEntity.getTelegramLink())
                .visibility(projectVisibilityToDomain(projectEntity.getVisibility()))
                .rewardSettings(
                        new ProjectRewardSettings(
                                projectEntity.getIgnorePullRequests(),
                                projectEntity.getIgnoreIssues(),
                                projectEntity.getIgnoreCodeReviews(),
                                projectEntity.getIgnoreContributionsBefore()
                        ))
                .topContributors(topContributors.stream().map(UserMapper::mapToContributorLinkView).collect(Collectors.toSet()))
                .contributorCount(contributorCount)
                .leaders(leaders.stream()
                        .filter(leader -> Boolean.TRUE.equals(leader.getHasAcceptedInvitation()))
                        .map(UserMapper::mapToProjectLeaderLinkView)
                        .collect(Collectors.toSet()))
                .invitedLeaders(leaders.stream()
                        .filter(leader -> Boolean.FALSE.equals(leader.getHasAcceptedInvitation()))
                        .map(UserMapper::mapToProjectLeaderLinkView)
                        .collect(Collectors.toSet()))
                .sponsors(sponsors.stream().map(SponsorMapper::mapToSponsorView).collect(Collectors.toSet()))
                .remainingUsdBudget(remainingUsdBudget)
                .build();

        for (ProjectOrganizationView organization : organizations) {
            project.addOrganization(organization);
        }
        return project;
    }

    static ProjectVisibility projectVisibilityToDomain(ProjectVisibilityEnumEntity visibility) {
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

    static ProjectVisibilityEnumEntity projectVisibilityToEntity(ProjectVisibility visibility) {
        switch (visibility) {
            case PUBLIC -> {
                return ProjectVisibilityEnumEntity.PUBLIC;
            }
            case PRIVATE -> {
                return ProjectVisibilityEnumEntity.PRIVATE;
            }
        }
        throw new IllegalArgumentException("Could not map project visibility");
    }

    static Project mapShortProjectViewToProject(ShortProjectViewEntity project) {
        return Project.builder()
                .id(project.getId())
                .slug(project.getKey())
                .name(project.getName())
                .shortDescription(project.getShortDescription())
                .longDescription(project.getLongDescription())
                .logoUrl(project.getLogoUrl())
                .hiring(project.getHiring())
                .visibility(projectVisibilityToDomain(project.getVisibility()))
                .build();
    }
}
