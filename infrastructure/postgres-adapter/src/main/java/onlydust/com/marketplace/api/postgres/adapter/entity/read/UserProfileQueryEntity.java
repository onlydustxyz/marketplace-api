package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.AllocatedTimeEnumEntity;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.project.domain.view.ContributorActivityView;
import onlydust.com.marketplace.project.domain.view.Money;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.project.domain.model.Contact.Channel;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Entity
@Accessors(fluent = true)
@Immutable
public class UserProfileQueryEntity {
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
    @Column(name = "html_url")
    String htmlUrl;
    @Column(name = "location")
    String location;
    @Column(name = "website")
    String website;
    @Column(name = "languages")
    String languages;
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

        public ContributorActivityView.ProfileStats.ContributionStats toDomain() {
            return ContributorActivityView.ProfileStats.ContributionStats.builder()
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
        @JsonProperty("currency_latest_usd_quote")
        BigDecimal currencyLatestUsdQuote;
        @JsonProperty("currency_logo_url")
        String logoUrl;

        public Money toDomain() {
            return new Money(totalAmount, CurrencyView.builder()
                    .id(CurrencyView.Id.of(currencyId))
                    .name(currencyName)
                    .code(currencyCode)
                    .decimals(currencyDecimals)
                    .latestUsdQuote(currencyLatestUsdQuote)
                    .logoUrl(logoUrl != null ? URI.create(logoUrl) : null)
                    .build())
                    .dollarsEquivalentValue(totalDollarsEquivalent);
        }
    }

    @Data
    @NoArgsConstructor
    public static class Contact {
        @JsonProperty("is_public")
        Boolean isPublic;
        @JsonProperty("channel")
        Channel channel;
        @JsonProperty("contact")
        String contact;
    }
}
