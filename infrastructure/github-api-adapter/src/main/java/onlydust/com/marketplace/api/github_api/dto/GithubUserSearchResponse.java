package onlydust.com.marketplace.api.github_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.util.List;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubUserSearchResponse {
    List<GithubUser> items;
}
