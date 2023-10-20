package onlydust.com.marketplace.api.domain.model;

import java.util.Date;

public record GithubRepo(
        Long id,
        String owner,
        String name,
        String htmlUrl,
        Date updatedAt,
        String description,
        Long starsCount,
        Long forksCount) {
}
