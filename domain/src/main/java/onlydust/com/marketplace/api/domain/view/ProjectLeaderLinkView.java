package onlydust.com.marketplace.api.domain.view;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectLeaderLinkView implements UserLinkView {

  UUID id;
  Long githubUserId;
  String login;
  String avatarUrl;
  String url;
}
