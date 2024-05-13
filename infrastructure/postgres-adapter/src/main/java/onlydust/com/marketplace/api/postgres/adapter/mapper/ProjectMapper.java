package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLeadViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectMoreInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.view.ProjectDetailsView;
import onlydust.com.marketplace.project.domain.view.ProjectOrganizationView;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface ProjectMapper {

    static ProjectDetailsView mapToProjectDetailsView(ProjectViewEntity projectEntity,
                                                      List<ContributorViewEntity> topContributors,
                                                      Integer contributorCount,
                                                      List<ProjectLeadViewEntity> leaders,
                                                      List<EcosystemEntity> ecosystems,
                                                      final Boolean hasRemainingBudget,
                                                      ProjectDetailsView.Me me) {

        final var organizationEntities = new HashMap<Long, GithubAccountEntity>();
        projectEntity.getRepos().forEach(repo -> organizationEntities.put(repo.getOwner().id(), repo.getOwner()));
        final var repoIdsIncludedInProject =
                projectEntity.getRepos().stream()
                        .filter(GithubRepoEntity::isPublic)
                        .map(GithubRepoEntity::getId).collect(Collectors.toSet());

        final var organizations = organizationEntities.values().stream().map(entity -> ProjectOrganizationView.builder()
                .id(entity.id())
                .login(entity.login())
                .avatarUrl(entity.avatarUrl())
                .htmlUrl(entity.htmlUrl())
                .name(entity.name())
                .installationId(isNull(entity.installation()) ? null : entity.installation().getId())
                .isInstalled(nonNull(entity.installation()))
                .repos(entity.repos().stream()
                        .filter(GithubRepoEntity::isPublic)
                        .map(repo -> RepoMapper.mapToDomain(repo,
                                repoIdsIncludedInProject.contains(repo.getId()),
                                entity.installation() != null &&
                                entity.installation().getAuthorizedRepos().stream()
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
                .slug(projectEntity.getSlug())
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
                .sponsors(projectEntity.getSponsors().stream().map(SponsorMapper::mapToSponsor).collect(Collectors.toSet()))
                .ecosystems(ecosystems.stream().map(EcosystemMapper::mapToDomain).collect(Collectors.toSet()))
                .hasRemainingBudget(hasRemainingBudget)
                .me(me)
                .tags(projectEntity.getTags().stream()
                        .map(projectTagEntity -> switch (projectTagEntity.getId().getTag()) {
                            case HOT_COMMUNITY -> Project.Tag.HOT_COMMUNITY;
                            case FAST_AND_FURIOUS -> Project.Tag.FAST_AND_FURIOUS;
                            case LIKELY_TO_REWARD -> Project.Tag.LIKELY_TO_REWARD;
                            case NEWBIES_WELCOME -> Project.Tag.NEWBIES_WELCOME;
                            case UPDATED_ROADMAP -> Project.Tag.UPDATED_ROADMAP;
                            case WORK_IN_PROGRESS -> Project.Tag.WORK_IN_PROGRESS;
                            case BIG_WHALE -> Project.Tag.BIG_WHALE;
                        }).collect(Collectors.toSet()))
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
                .slug(project.getSlug())
                .name(project.getName())
                .shortDescription(project.getShortDescription())
                .longDescription(project.getLongDescription())
                .logoUrl(project.getLogoUrl())
                .hiring(project.getHiring())
                .visibility(projectVisibilityToDomain(project.getVisibility()))
                .build();
    }

    static List<NamedLink> mapMoreInfosWithDefaultValue(final ProjectViewEntity projectViewEntity) {
        return projectViewEntity.getMoreInfos().stream()
                .sorted(Comparator.comparing(ProjectMoreInfoEntity::getRank))
                .map(projectMoreInfoEntity -> NamedLink.builder()
                        .value(projectMoreInfoEntity.getName())
                        .url(projectMoreInfoEntity.getUrl()).build())
                .toList();

    }

    static Set<ProjectMoreInfoEntity> moreInfosToEntities(final List<NamedLink> moreInfos, final UUID projectId) {
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
