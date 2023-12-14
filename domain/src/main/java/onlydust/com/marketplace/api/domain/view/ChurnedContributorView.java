package onlydust.com.marketplace.api.domain.view;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.*;

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

    @Data
    @Builder
    public static class Contribution {
        String id;
        ShortRepoView repo;
        ZonedDateTime completedAt;
    }
}
