package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.AllUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserProfileInfoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountViewEntity;
import onlydust.com.marketplace.bff.read.mapper.ContactMapper;
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

@Entity
@Value
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

    @OneToOne
    @JoinColumn(name = "githubUserId", insertable = false, updatable = false)
    @NonNull
    AllUserViewEntity user;

    @NonNull Integer rank;
    @NonNull BigDecimal rankPercentile;
    @Enumerated(EnumType.STRING)
    @NonNull
    UserRankCategory rankCategory;
    @NonNull Integer contributedProjectCount;
    @NonNull Integer leadedProjectCount;
    @NonNull Integer contributionCount;
    @NonNull Integer rewardCount;

    @JdbcTypeCode(SqlTypes.JSON)
    List<Ecosystem> ecosystems;

    public PublicUserProfileResponseV2 toDto() {
        return new PublicUserProfileResponseV2()
                .githubUserId(githubUserId)
                .login(user().login())
                .avatarUrl(user().avatarUrl())
                .id(user.userId())
                .htmlUrl(URI.create(user.github().htmlUrl()))
                .location(Optional.ofNullable(user.profile()).map(UserProfileInfoViewEntity::location).orElse(user.github().location()))
                .bio(Optional.ofNullable(user.profile()).map(UserProfileInfoViewEntity::bio).orElse(user.github().bio()))
                .website(Optional.ofNullable(user.profile()).map(UserProfileInfoViewEntity::website).orElse(user.github().website()))
                .signedUpOnGithubAt(user.github().createdAt())
                .signedUpAt(Optional.ofNullable(user.registered()).map(UserViewEntity::createdAt).map(d -> d.toInstant().atZone(ZoneOffset.UTC)).orElse(null))
                .lastSeenAt(Optional.ofNullable(user.registered()).map(UserViewEntity::lastSeenAt).orElse(null))
                .contacts(Optional.ofNullable(user.profile()).flatMap(UserProfileInfoViewEntity::publicContacts).map(l -> l.stream().map(ContactMapper::map).toList()).orElse(contactsOf(user.github())))
                .statsSummary(new UserProfileStatsSummary()
                        .rank(rank)
                        .rankPercentile(prettyRankPercentile(rankPercentile))
                        .rankCategory(rankCategory)
                        .contributedProjectCount(contributedProjectCount)
                        .leadedProjectCount(leadedProjectCount)
                        .contributionCount(contributionCount)
                        .rewardCount(rewardCount))
                .ecosystems(ecosystems.stream()
                        .map(ecosystem -> new EcosystemResponse()
                                .id(ecosystem.id())
                                .name(ecosystem.name())
                                .url(ecosystem.url())
                                .logoUrl(ecosystem.logoUrl())
                                .bannerUrl(ecosystem.bannerUrl())
                        ).toList());
    }

    public static BigDecimal prettyRankPercentile(BigDecimal rankPercentile) {
        final var percent = rankPercentile.multiply(BigDecimal.valueOf(100)).doubleValue();
        return List.of(0.1D, 1D, 5D, 10D).stream()
                .filter(i -> percent <= i)
                .map(BigDecimal::valueOf)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.valueOf(100));
    }

    private static List<ContactInformation> contactsOf(GithubAccountViewEntity account) {
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

    record Ecosystem(UUID id, String name, String url, String logoUrl, String bannerUrl) {
    }
}
