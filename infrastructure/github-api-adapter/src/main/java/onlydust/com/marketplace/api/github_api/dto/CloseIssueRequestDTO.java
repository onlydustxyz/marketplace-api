package onlydust.com.marketplace.api.github_api.dto;

import lombok.Builder;

@Builder
public class CloseIssueRequestDTO {
    @Builder.Default
    String state = "closed";
}
