package onlydust.com.marketplace.api.github_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubOrgaSearchResponseDTO {

  String login;
  Long id;
  String url;
  @JsonProperty("avatar_url")
  String avatarUrl;
}
