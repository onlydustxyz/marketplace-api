package onlydust.com.marketplace.bff.read.entities.user;

import lombok.Data;

import java.util.UUID;

@Data
public class UserLinkJson {
    UUID id;
    Long githubUserId;
    String login;
    String avatarUrl;
}