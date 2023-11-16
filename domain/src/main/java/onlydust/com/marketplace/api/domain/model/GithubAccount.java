package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class GithubAccount {
    Long id;
    String login;
    String name;
    String type;
    String htmlUrl;
    String avatarUrl;
    @Builder.Default
    List<GithubRepo> repos = new ArrayList<>();
    @Builder.Default
    Boolean installed = false;
}
