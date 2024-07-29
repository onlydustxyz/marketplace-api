package onlydust.com.marketplace.user.domain.model;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true, chain = true)
public class User {
    UserId id;
    String email;
    String login;
}
