package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.AllocatedTimeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ContactChanelEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProfileCoverEnumEntity;
import onlydust.com.marketplace.project.domain.view.CurrencyView;
import onlydust.com.marketplace.project.domain.view.UserProfileView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
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
@TypeDef(name = "contact_channel", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "allocated_time", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "profile_cover", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
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
    @Type(type = "profile_cover")
    @Column(name = "cover")
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
    @Type(type = "allocated_time")
    @Column(name = "weekly_allocated_time")
    AllocatedTimeEnumEntity allocatedTimeToContribute;


    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "contacts")
    private List<Contact> contacts;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "counts")
    private List<WeekCount> counts;

    @Type(type = "jsonb")
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

        public onlydust.com.marketplace.project.domain.view.TotalEarnedPerCurrency toDomain() {
            return onlydust.com.marketplace.project.domain.view.TotalEarnedPerCurrency.builder()
                    .totalDollarsEquivalent(totalDollarsEquivalent)
                    .totalAmount(totalAmount)
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
