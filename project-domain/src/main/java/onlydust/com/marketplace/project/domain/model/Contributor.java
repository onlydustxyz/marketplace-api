package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;

@Data
@Builder(toBuilder = true)
public class Contributor {
    GithubUserIdentity id;
    Boolean isRegistered;
    UserId userId;
}
