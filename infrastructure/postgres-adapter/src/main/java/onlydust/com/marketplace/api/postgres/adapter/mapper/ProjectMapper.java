package onlydust.com.marketplace.api.postgres.adapter.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.sql.Date;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import onlydust.com.marketplace.api.domain.model.MoreInfoLink;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;
import onlydust.com.marketplace.api.domain.view.ProjectOrganizationView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLeadViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectMoreInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;

public interface ProjectMapper {

  static ProjectDetailsView mapToProjectDetailsView(ProjectViewEntity projectEntity,
      List<ContributorViewEntity> topContributors,
      Integer contributorCount,
      List<ProjectLeadViewEntity> leaders,
      List<SponsorEntity> sponsors,
      final Boolean hasRemainingBudget,
      ProjectDetailsView.Me me) {

    final var organizationEntities = new HashMap<Long, GithubAccountEntity>();
    projectEntity.getRepos().forEach(repo -> organizationEntities.put(repo.getOwner().getId(), repo.getOwner()));
    final var repoIdsIncludedInProject =
        projectEntity.getRepos().stream()
            .filter(GithubRepoEntity::isPublic)
            .map(GithubRepoEntity::getId).collect(Collectors.toSet());

    final var organizations = organizationEntities.values().stream().map(entity -> ProjectOrganizationView.builder()
        .id(entity.getId())
        .login(entity.getLogin())
        .avatarUrl(entity.getAvatarUrl())
        .htmlUrl(entity.getHtmlUrl())
        .name(entity.getName())
        .installationId(isNull(entity.getInstallation()) ? null : entity.getInstallation().getId())
        .isInstalled(nonNull(entity.getInstallation()))
        .repos(entity.getRepos().stream()
            .filter(GithubRepoEntity::isPublic)
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
        .moreInfos(mapMoreInfosWithDefaultValue(projectEntity))
        .visibility(projectVisibilityToDomain(projectEntity.getVisibility()))
        .rewardSettings(
            new ProjectRewardSettings(
                projectEntity.getIgnorePullRequests(),
                projectEntity.getIgnoreIssues(),
                projectEntity.getIgnoreCodeReviews(),
                projectEntity.getIgnoreContributionsBefore()
            ))
        .topContributors(topContributors.stream().map(UserMapper::mapToContributorLinkView).toList())
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
        .hasRemainingBudget(hasRemainingBudget)
        .me(me)
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

  static List<MoreInfoLink> mapMoreInfosWithDefaultValue(final ProjectViewEntity projectViewEntity) {
    if (isNull(projectViewEntity.getMoreInfos()) || projectViewEntity.getMoreInfos().isEmpty()) {
      if (nonNull(projectViewEntity.getTelegramLink())) {
        return List.of(MoreInfoLink.builder()
            .url(projectViewEntity.getTelegramLink())
            .build());
      }
    } else {
      return projectViewEntity.getMoreInfos().stream()
          .sorted(Comparator.comparing(ProjectMoreInfoEntity::getRank))
          .map(projectMoreInfoEntity -> MoreInfoLink.builder()
              .value(projectMoreInfoEntity.getName())
              .url(projectMoreInfoEntity.getUrl()).build())
          .toList();
    }
    return List.of();
  }

  static Set<ProjectMoreInfoEntity> moreInfosToEntities(final List<MoreInfoLink> moreInfos, final UUID projectId) {
    final Set<ProjectMoreInfoEntity> entities = new HashSet<>();
    for (int i = 0; i < moreInfos.size(); i++) {
      final var moreInfo = moreInfos.get(i);
      entities.add(ProjectMoreInfoEntity.builder()
          .projectId(projectId)
          .url(moreInfo.getUrl())
          .name(moreInfo.getValue())
          .rank(i)
          .build());
    }
    return entities;
  }
}
