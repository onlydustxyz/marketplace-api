package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.project.domain.model.UserProfileCover;

import java.time.ZonedDateTime;

@Data
@Builder
public class ChurnedContributorView {
    Long githubId;
    String login;
    String htmlUrl;
    String avatarUrl;
    Boolean isRegistered;
    UserProfileCover cover;
    Contribution lastContribution;

    public UserProfileCover getCover() {
        return cover == null ? UserProfileCover.get(githubId) : cover;
    }

    @Data
    @Builder
    public static class Contribution {
        String id;
        ShortRepoView repo;
        ZonedDateTime completedAt;
    }
}
