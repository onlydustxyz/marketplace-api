package onlydust.com.marketplace.project.domain.view;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import onlydust.com.marketplace.project.domain.model.Contact;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.model.UserAllocatedTimeToContribute;
import onlydust.com.marketplace.project.domain.model.UserProfileCover;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
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
    Map<String, Long> technologies;
    UserProfileCover cover;
    ProfileStats profileStats;
    UserAllocatedTimeToContribute allocatedTimeToContribute;
    Boolean isLookingForAJob;
    @Builder.Default
    @Setter(AccessLevel.NONE)
    Set<ProjectStats> projectsStats = new HashSet<>();
    @Builder.Default
    Set<Contact> contacts = new HashSet<>();
    String firstName;
    String lastName;

    public void addProjectStats(final ProjectStats projectStats) {
        if (projectStats.getUserFirstContributedAt() != null && (firstContributedAt == null || projectStats.getUserFirstContributedAt().before(firstContributedAt))) {
            firstContributedAt = projectStats.getUserFirstContributedAt();
        }
        this.projectsStats.add(projectStats);
    }

    public UserProfileCover getCover() {
        return cover == null ? UserProfileCover.get(githubId) : cover;
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
        Date projectLeadSince;
        ProjectVisibility visibility;
    }

    @Data
    @Builder
    public static class ProfileStats {
        Integer contributedProjectCount;
        Integer leadedProjectCount;
        TotalsEarned totalsEarned;
        Integer contributionCount;
        @Builder.Default
        List<ContributionStats> contributionStats = new ArrayList<>();

        public int getContributionCountVariationSinceLastWeek() {
            LocalDate currentWeek = LocalDate.now();
            LocalDate previousWeek = LocalDate.now().minusWeeks(1);
            final var currentWeekWithStats = contributionStats.stream()
                    .filter(stats -> stats.getYear() == currentWeek.getYear() && stats.getWeek() == currentWeek.get(WeekFields.of(Locale.getDefault()).weekOfYear())).findFirst();
            final var previousWeekWithStats = contributionStats.stream()
                    .filter(stats -> stats.getYear() == previousWeek.getYear() && stats.getWeek() == previousWeek.get(WeekFields.of(Locale.getDefault()).weekOfYear())).findFirst();
            final int currentWeekCount = currentWeekWithStats.map(ContributionStats::getTotalCount).orElse(0);
            final int previousWeekCount = previousWeekWithStats.map(ContributionStats::getTotalCount).orElse(0);
            return currentWeekCount - previousWeekCount;
        }

        @Data
        @Builder
        public static class ContributionStats {
            int year;
            int week;
            int codeReviewCount;
            int issueCount;
            int pullRequestCount;

            public int getTotalCount() {
                return codeReviewCount + issueCount + pullRequestCount;
            }
        }

        public static class ContributionStatsComparator implements Comparator<ContributionStats> {
            @Override
            public int compare(ContributionStats o1, ContributionStats o2) {
                final int yearComparison = Integer.compare(o1.getYear(), o2.getYear());
                return yearComparison == 0 ? Integer.compare(o1.getWeek(), o2.getWeek()) : yearComparison;
            }
        }
    }
}
