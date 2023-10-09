package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RepoCardView {
    Long githubRepoId;
    String owner;
    String name;
    String description;
    Integer starCount;
    Integer forkCount;
    String url;
    Boolean hasIssues;
}
