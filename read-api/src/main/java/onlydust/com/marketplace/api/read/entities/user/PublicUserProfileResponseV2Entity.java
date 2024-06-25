package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.github.GithubAccountReadEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class PublicUserProfileResponseV2Entity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long githubUserId;

    UUID userId;
    @NonNull
    String login;
    @NonNull
    String avatarUrl;
    String email;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    UserReadEntity registered;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "githubUserId", insertable = false, updatable = false)
    @NonNull
    GithubAccountReadEntity github;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    UserProfileInfoReadEntity profile;

    @NonNull
    Integer rank;
    @NonNull
    BigDecimal rankPercentile;
    @Enumerated(EnumType.STRING)
    @NonNull
    UserRankCategory rankCategory;
    @NonNull
    Integer contributedProjectCount;
    @NonNull
    Integer leadedProjectCount;
    @NonNull
    Integer contributionCount;
    @NonNull
    Integer rewardCount;

    @JdbcTypeCode(SqlTypes.JSON)
    List<Ecosystem> ecosystems;

    public PublicUserProfileResponseV2 toDto() {
        return new PublicUserProfileResponseV2()
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .id(userId)
                .htmlUrl(isNull(github) ? null : URI.create(github.htmlUrl()))
                .location(Optional.ofNullable(profile).map(UserProfileInfoReadEntity::location).orElse(isNull(github) ? null : github.location()))
                .bio(Optional.ofNullable(profile).map(UserProfileInfoReadEntity::bio).orElse(isNull(github) ? null : github.bio()))
                .website(Optional.ofNullable(profile).map(UserProfileInfoReadEntity::website).orElse(isNull(github) ? null : github.website()))
                .signedUpOnGithubAt(isNull(github) ? null : github.createdAt())
                .signedUpAt(Optional.ofNullable(registered).map(UserReadEntity::createdAt).map(d -> d.toInstant().atZone(ZoneOffset.UTC)).orElse(null))
                .lastSeenAt(Optional.ofNullable(registered).map(UserReadEntity::lastSeenAt).orElse(null))
                .contacts(Optional.ofNullable(profile).flatMap(UserProfileInfoReadEntity::publicContacts)
                        .map(l -> l.stream().map(ContactInformationReadEntity::toDto).toList())
                        .orElse(isNull(github) ? List.of() : contactsOf(github)))
                .statsSummary(new UserProfileStatsSummary()
                        .rank(Optional.ofNullable(rank).orElse(0))
                        .rankPercentile(prettyRankPercentile(Optional.ofNullable(rankPercentile).orElse(BigDecimal.ONE)))
                        .rankCategory(Optional.ofNullable(rankCategory).orElse(UserRankCategory.F))
                        .contributedProjectCount(Optional.ofNullable(contributedProjectCount).orElse(0))
                        .leadedProjectCount(Optional.ofNullable(leadedProjectCount).orElse(0))
                        .contributionCount(Optional.ofNullable(contributionCount).orElse(0))
                        .rewardCount(Optional.ofNullable(rewardCount).orElse(0)))
                .ecosystems(isNull(ecosystems) ? List.of() : ecosystems.stream()
                        .map(ecosystem -> new EcosystemResponse()
                                .id(ecosystem.id())
                                .name(ecosystem.name())
                                .url(ecosystem.url())
                                .logoUrl(ecosystem.logoUrl())
                                .bannerUrl(ecosystem.bannerUrl())
                                .slug(ecosystem.slug())
                        ).toList());
    }

    public static BigDecimal prettyRankPercentile(BigDecimal rankPercentile) {
        final var percent = rankPercentile.multiply(BigDecimal.valueOf(100)).doubleValue();
        return Stream.of(0.1D, 1D, 5D, 10D)
                .filter(i -> percent <= i)
                .map(BigDecimal::valueOf)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.valueOf(100));
    }

    private static List<ContactInformation> contactsOf(GithubAccountReadEntity account) {
        return Stream.of(
                        account.twitter() == null ? null :
                                new ContactInformation().channel(ContactInformationChannel.TWITTER).contact(account.twitter()).visibility(ContactInformation.VisibilityEnum.PUBLIC),
                        account.linkedin() == null ? null :
                                new ContactInformation().channel(ContactInformationChannel.LINKEDIN).contact(account.linkedin()).visibility(ContactInformation.VisibilityEnum.PUBLIC),
                        account.telegram() == null ? null :
                                new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact(account.telegram()).visibility(ContactInformation.VisibilityEnum.PUBLIC)
                ).filter(Objects::nonNull)
                .toList();
    }

    record Ecosystem(UUID id, String name, String url, String logoUrl, String bannerUrl, String slug) {
    }
}
