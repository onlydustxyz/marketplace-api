package onlydust.com.marketplace.api.domain.view;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import onlydust.com.marketplace.api.domain.model.Contact;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.UserAllocatedTimeToContribute;
import onlydust.com.marketplace.api.domain.model.UserProfileCover;

import java.math.BigDecimal;
import java.util.*;

@Data
@Builder
public class UserProfileView {
    UUID id;
    Long githubId;
    String bio;
    String avatarUrl;
    String login;
    String website;
    String htmlUrl;
    Date createAt;
    Date lastSeenAt;
    Date firstContributedAt;
    String location;
    String twitter;
    String linkedin;
    String github;
    String telegram;
    String discord;
    Map<String, Integer> technologies;
    UserProfileCover cover;
    ProfileStats profileStats;
    UserAllocatedTimeToContribute allocatedTimeToContribute;
    Boolean isLookingForAJob;
    @Builder.Default
    @Setter(AccessLevel.NONE)
    Set<ProjectStats> projectsStats = new HashSet<>();
    @Builder.Default
    Set<Contact> contacts = new HashSet<>();

    public void addProjectStats(final ProjectStats projectStats) {
        if (projectStats.getUserFirstContributedAt() != null && (firstContributedAt == null || projectStats.getUserFirstContributedAt().before(firstContributedAt))) {
            firstContributedAt = projectStats.getUserFirstContributedAt();
        }
        this.projectsStats.add(projectStats);
    }

    @Data
    @Builder
    public static class ProjectStats {
        UUID id;
        String slug;
        String name;
        String logoUrl;
        Integer contributorCount;
        BigDecimal totalGranted;
        Integer userContributionCount;
        Date userLastContributedAt;
        Date userFirstContributedAt;
        Boolean isProjectLead;
    }

    @Data
    @Builder
    public static class ProfileStats {
        Integer contributedProjectCount;
        Integer leadedProjectCount;
        TotalsEarned totalsEarned;
        Integer contributionCount;
        @Builder.Default
        Set<ContributionStats> contributionStats = new HashSet<>();

        @Data
        @Builder
        public static class ContributionStats {
            Integer year;
            Integer week;
            Integer codeReviewCount;
            Integer issueCount;
            Integer pullRequestCount;
        }
    }

    @Data
    @Builder
    public static class TotalsEarned {
        BigDecimal totalDollarsEquivalent;
        List<TotalEarnedPerCurrency> details;
    }

    @Data
    @Builder
    public static class TotalEarnedPerCurrency {
        BigDecimal totalDollarsEquivalent;
        BigDecimal totalAmount;
        Currency currency;
    }
}
