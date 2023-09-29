package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProjectLeadView {
    UUID id;
    String login;
    Integer githubUserId;
    String url;
    String avatarUrl;
}
