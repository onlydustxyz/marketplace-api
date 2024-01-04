package onlydust.com.marketplace.api.github_api.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CloseIssueRequestDTO {

  @Builder.Default
  String state = "closed";
}
