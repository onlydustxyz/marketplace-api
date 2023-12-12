package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Value
@Builder(toBuilder = true)
@EqualsAndHashCode
public class GithubRepo {
    Long id;
    String owner;
    String name;
    String htmlUrl;
    Date updatedAt;
    String description;
    Long starsCount;
    Long forksCount;
    @Builder.Default
    Map<String, Long> technologies = new HashMap<>();
}
