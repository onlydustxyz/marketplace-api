package onlydust.com.marketplace.api.domain.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

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
    String location;
    String twitter;
    String linkedin;
    String github;
    String telegram;
    String discord;
    Map<String, Integer> technologies;
    Cover cover;
    ProfileStats profileStats;
    @Builder.Default
    Set<ProjectStats> projectsStats = new HashSet<>();
    @Builder.Default
    Set<ContactInformation> contactInformations = new HashSet<>();

    public void addContactInformation(final ContactInformation contactInformation) {
        this.contactInformations.add(contactInformation);
    }

    public void addProjectStats(final ProjectStats projectStats) {
        this.projectsStats.add(projectStats);
    }

    public enum Cover {
        MAGENTA, CYAN, BLUE, YELLOW
    }

    @Data
    @Builder
    public static class ProjectStats {
        UUID id;
        String name;
        String logoUrl;
        Integer contributorCount;
        BigDecimal totalGranted;
        Integer userContributionCount;
        Date userLastContributedAt;
        Boolean isProjectLead;
    }

    @Data
    @Builder
    public static class ProfileStats {
        Integer contributedProjectCount;
        Integer leadedProjectCount;
        BigDecimal totalEarned;
        Integer contributionCount;
        @Builder.Default
        Set<ContributionStats> contributionStats = new HashSet<>();

        public void addContributionStat(final ContributionStats contributionStats) {
            this.contributionStats.add(contributionStats);
        }

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
    public static class ContactInformation {
        String channel;
        String contact;
        Visibility visibility;

        @AllArgsConstructor
        @Getter
        public enum Visibility {
            PRIVATE("private"), PUBLIC("public");
            final String value;
        }
    }


}
