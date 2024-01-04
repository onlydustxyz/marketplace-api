package onlydust.com.marketplace.api.domain.model;

import java.util.Date;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

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
