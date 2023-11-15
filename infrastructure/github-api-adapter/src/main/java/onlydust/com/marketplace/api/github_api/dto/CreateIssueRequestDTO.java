package onlydust.com.marketplace.api.github_api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateIssueRequestDTO {
    String title;
    String body;
}
