package onlydust.com.marketplace.api.postgres.adapter.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.view.RepoCardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.GithubRepoViewEntity;

import java.util.HashMap;

import static java.util.Objects.isNull;

public interface RepoMapper {

    static RepoCardView mapToRepoCardView(GithubRepoViewEntity repo) {
        return RepoCardView.builder()
                .githubRepoId(repo.getGithubId())
                .owner(repo.getOwner())
                .name(repo.getName())
                .description(repo.getDescription())
                .forkCount(repo.getForkCount())
                .starCount(repo.getStarCount())
                .url(repo.getHtmlUrl())
                .hasIssues(repo.getHasIssues())
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
