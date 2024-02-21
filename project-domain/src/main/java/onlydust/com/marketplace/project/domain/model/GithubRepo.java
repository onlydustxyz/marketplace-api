package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Date;

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
}
