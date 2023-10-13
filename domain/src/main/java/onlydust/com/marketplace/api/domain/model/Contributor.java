package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Contributor {
    GithubUserIdentity id;
    Boolean isRegistered;
}
