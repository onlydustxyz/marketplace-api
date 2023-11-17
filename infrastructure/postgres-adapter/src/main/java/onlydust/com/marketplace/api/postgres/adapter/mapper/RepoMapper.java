package onlydust.com.marketplace.api.postgres.adapter.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.view.ProjectOrganizationRepoView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.GithubRepoViewEntity;

import java.util.HashMap;

import static java.util.Objects.isNull;

public interface RepoMapper {

    static ProjectOrganizationRepoView mapToDomain(GithubRepoEntity repo, boolean isIncludedInProject) {
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
                .technologies(repo.getLanguages())
                .build();
    }

    static HashMap<String, Integer> mapLanguages(GithubRepoViewEntity repo) {
        if (isNull(repo.getLanguages())) {
            return new HashMap<>();
        }
        TypeReference<HashMap<String, Integer>> typeRef = new TypeReference<>() {
        };
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(repo.getLanguages(), typeRef);
        } catch (JsonProcessingException e) {
            throw OnlyDustException.internalServerError("Failed to parse languages", e);
        }
    }
}
