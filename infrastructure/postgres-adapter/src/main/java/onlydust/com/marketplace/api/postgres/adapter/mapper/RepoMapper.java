package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.view.ProjectOrganizationRepoView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoStatsEntity;

import java.util.Optional;

public interface RepoMapper {

    static ProjectOrganizationRepoView mapToDomain(GithubRepoEntity repo, boolean isIncludedInProject,
                                                   boolean isAuthorizedInGithubApp) {
        return ProjectOrganizationRepoView.builder()
                .githubRepoId(repo.getId())
                .owner(repo.getOwner().getLogin())
                .name(repo.getName())
                .description(repo.getDescription())
                .forkCount(repo.getForksCount())
                .starCount(repo.getStarsCount())
                .url(repo.getHtmlUrl())
                .hasIssues(repo.getHasIssues())
                .isIncludedInProject(isIncludedInProject)
                .isAuthorizedInGithubApp(isAuthorizedInGithubApp)
                .technologies(repo.getLanguages())
                .indexedAt(Optional.ofNullable(repo.getStats()).map(GithubRepoStatsEntity::getLastIndexedAt).orElse(null))
                .build();
    }

}
