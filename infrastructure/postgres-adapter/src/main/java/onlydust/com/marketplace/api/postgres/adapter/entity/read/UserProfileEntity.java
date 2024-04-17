package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.AllocatedTimeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ContactChanelEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProfileCoverEnumEntity;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.project.domain.view.Money;
import onlydust.com.marketplace.project.domain.view.UserProfileView;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
public class UserProfileEntity {
    @Id
    @Column(name = "github_user_id", nullable = false)
    Long githubId;

    @Column(name = "id")
    UUID id;
    @Column(name = "email")
    String email;
    @Column(name = "bio")
    String bio;
    @Column(name = "avatar_url", nullable = false)
    String avatarUrl;
    @Column(name = "login", nullable = false)
    String login;
    @Column(name = "html_url", nullable = false)
    String htmlUrl;
    @Column(name = "location")
    String location;
    @Column(name = "website")
    String website;
    @Column(name = "languages")
    String languages;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "profile_cover")
    ProfileCoverEnumEntity cover;
    @Column(name = "last_seen_at")
    Date lastSeenAt;
    @Column(name = "created_at")
    Date createdAt;
    @Column(name = "leading_project_number")
    Integer numberOfLeadingProject;
    @Column(name = "contributor_on_project")
    Integer numberOfOwnContributorOnProject;
    @Column(name = "contributions_count")
    Integer contributionsCount;
    @Column(name = "looking_for_a_job")
    Boolean isLookingForAJob;
    @Column(name = "first_name")
    String firstName;
    @Column(name = "last_name")
    String lastName;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "allocated_time", name = "weekly_allocated_time")
    AllocatedTimeEnumEntity allocatedTimeToContribute;


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "contacts")
    private List<Contact> contacts;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "counts")
    private List<WeekCount> counts;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "totals_earned")
    private List<TotalEarnedPerCurrency> totalEarnedPerCurrencies;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekCount {
        @JsonProperty("code_review_count")
        Integer codeReviewCount;
        @JsonProperty("issue_count")
        Integer issueCount;
        @JsonProperty("pull_request_count")
        Integer pullRequestCount;
        @JsonProperty("week")
        Integer week;
        @JsonProperty("year")
        Integer year;

        public UserProfileView.ProfileStats.ContributionStats toDomain() {
            return UserProfileView.ProfileStats.ContributionStats.builder()
                    .codeReviewCount(codeReviewCount)
                    .issueCount(issueCount)
                    .pullRequestCount(pullRequestCount)
                    .week(week)
                    .year(year)
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalEarnedPerCurrency {
        @JsonProperty("total_dollars_equivalent")
        BigDecimal totalDollarsEquivalent;
        @JsonProperty("total_amount")
        BigDecimal totalAmount;
        @JsonProperty("currency_id")
        UUID currencyId;
        @JsonProperty("currency_code")
        String currencyCode;
        @JsonProperty("currency_name")
        String currencyName;
        @JsonProperty("currency_decimals")
        Integer currencyDecimals;
        @JsonProperty("currency_logo_url")
        String logoUrl;

        public Money toDomain() {
            return Money.builder()
                    .usdEquivalent(totalDollarsEquivalent)
                    .amount(totalAmount)
                    .currency(CurrencyView.builder()
                            .id(CurrencyView.Id.of(currencyId))
                            .name(currencyName)
                            .code(currencyCode)
                            .decimals(currencyDecimals)
                            .logoUrl(logoUrl != null ? URI.create(logoUrl) : null)
                            .build())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        @JsonProperty("is_public")
        Boolean isPublic;
        @JsonProperty("channel")
        ContactChanelEnumEntity channel;
        @JsonProperty("contact")
        String contact;
    }
}
