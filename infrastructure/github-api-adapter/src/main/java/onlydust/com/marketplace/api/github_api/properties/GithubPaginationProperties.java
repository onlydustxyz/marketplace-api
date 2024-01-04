package onlydust.com.marketplace.api.github_api.properties;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GithubPaginationProperties {

  @Builder.Default
  int pageSize = 100;
}
