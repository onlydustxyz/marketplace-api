package onlydust.com.marketplace.api.github_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubUser {
    Long id;
    String login;
    @JsonProperty("avatar_url")
    String avatarUrl;
}
