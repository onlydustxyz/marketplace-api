package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoLanguageViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoStatsViewEntity;
import onlydust.com.marketplace.project.domain.view.ProjectOrganizationRepoView;

import java.util.Optional;
import java.util.stream.Collectors;

public interface RepoMapper {

    static ProjectOrganizationRepoView mapToDomain(GithubRepoViewEntity repo, boolean isIncludedInProject,
                                                   boolean isAuthorizedInGithubApp) {
        return ProjectOrganizationRepoView.builder()
                .githubRepoId(repo.getId())
                .owner(repo.getOwner().login())
                .name(repo.getName())
                .description(repo.getDescription())
                .forkCount(repo.getForksCount())
                .starCount(repo.getStarsCount())
                .url(repo.getHtmlUrl())
                .hasIssues(repo.getHasIssues())
                .isIncludedInProject(isIncludedInProject)
                .isAuthorizedInGithubApp(isAuthorizedInGithubApp)
                .technologies(repo.getLanguages().stream().collect(Collectors.toMap(GithubRepoLanguageViewEntity::getLanguage,
                        GithubRepoLanguageViewEntity::getLineCount)))
                .indexedAt(Optional.ofNullable(repo.getStats()).map(GithubRepoStatsViewEntity::getLastIndexedAt).orElse(null))
                .build();
    }

}
