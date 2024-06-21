package onlydust.com.marketplace.api.github_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicantGrantDTO{
    @JsonProperty("access_token")
    String accessToken;
}
