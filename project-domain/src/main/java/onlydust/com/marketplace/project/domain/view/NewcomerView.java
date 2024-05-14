package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.project.domain.model.UserProfileCover;

import java.time.ZonedDateTime;

@Data
@Builder
public class NewcomerView {
    Long githubId;
    String login;
    String htmlUrl;
    String avatarUrl;
    Boolean isRegistered;
    String location;
    String bio;
    ZonedDateTime firstContributedAt;

    public UserProfileCover getCover() {
        return UserProfileCover.get(githubId);
    }
}
