package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@ToString
public class User {
    UUID id;
    @Builder.Default
    List<String> permissions = new ArrayList<>();
    Long githubUserId;
    String avatarUrl;
    String login;
}
