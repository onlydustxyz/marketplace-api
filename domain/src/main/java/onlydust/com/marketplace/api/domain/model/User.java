package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class User {
    UUID id;
    List<String> permissions;
    Long githubUserId;
    String avatarUrl;
    String login;
}
