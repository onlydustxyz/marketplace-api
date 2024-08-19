package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Data
@Builder
public class ContributorActivityView {
    Long githubId;
    String login;
    String htmlUrl;
    String avatarUrl;
    Boolean isRegistered;
    Integer completedPullRequestCount;
    Integer completedIssueCount;
    Integer completedCodeReviewCount;
    @Builder.Default
    List<ProfileStats.ContributionStats> contributionStats = new ArrayList<>();

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
