package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Contact;
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
    Set<ProjectStats> projectsStats = new HashSet<>();
    @Builder.Default
    Set<Contact> contacts = new HashSet<>();

    public void addContactInformation(final Contact contact) {
        this.contacts.add(contact);
    }

    public void addProjectStats(final ProjectStats projectStats) {
        this.projectsStats.add(projectStats);
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
}
