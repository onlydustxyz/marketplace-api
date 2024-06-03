package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectMoreInfoEntity;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.project.domain.view.ProjectDetailsView;
import onlydust.com.marketplace.project.domain.view.ProjectOrganizationView;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface ProjectMapper {

    static ProjectDetailsView mapToProjectDetailsView(ProjectViewEntity projectEntity,
                                                      List<ContributorQueryEntity> topContributors,
                                                      Integer contributorCount,
                                                      List<ProjectLeadQueryEntity> leaders,
                                                      List<EcosystemEntity> ecosystems,
                                                      final Boolean hasRemainingBudget,
                                                      ProjectDetailsView.Me me) {

        final var organizationEntities = new HashMap<Long, GithubAccountViewEntity>();
        projectEntity.getRepos().forEach(repo -> organizationEntities.put(repo.getOwner().id(), repo.getOwner()));
        final var repoIdsIncludedInProject =
                projectEntity.getRepos().stream()
                        .filter(GithubRepoViewEntity::isPublic)
                        .map(GithubRepoViewEntity::getId).collect(Collectors.toSet());

        final var organizations = organizationEntities.values().stream().map(entity -> ProjectOrganizationView.builder()
                .id(entity.id())
                .login(entity.login())
                .avatarUrl(entity.avatarUrl())
                .htmlUrl(entity.htmlUrl())
                .name(entity.name())
                .installationId(isNull(entity.installation()) ? null : entity.installation().getId())
                .isInstalled(nonNull(entity.installation()))
                .repos(entity.repos().stream()
                        .filter(GithubRepoViewEntity::isPublic)
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
                .visibility(projectEntity.getVisibility())
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
                        .map(projectTagEntity -> projectTagEntity.getTag()).collect(Collectors.toSet()))
                .build();

        for (ProjectOrganizationView organization : organizations) {
            project.addOrganization(organization);
        }
        return project;
    }

    static Project mapShortProjectViewToProject(ShortProjectQueryEntity project) {
        return Project.builder()
                .id(project.getId())
                .slug(project.getSlug())
                .name(project.getName())
                .shortDescription(project.getShortDescription())
                .longDescription(project.getLongDescription())
                .logoUrl(project.getLogoUrl())
                .hiring(project.getHiring())
                .visibility(project.getVisibility())
                .build();
    }

    static List<NamedLink> mapMoreInfosWithDefaultValue(final ProjectViewEntity projectViewEntity) {
        return projectViewEntity.getMoreInfos().stream()
                .sorted(Comparator.comparing(ProjectMoreInfoViewEntity::getRank))
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
