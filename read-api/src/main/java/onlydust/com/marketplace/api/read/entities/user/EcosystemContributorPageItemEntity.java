package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.UserRankCategory;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@IdClass(EcosystemContributorPageItemEntity.PrimaryKey.class)
public class EcosystemContributorPageItemEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID ecosystemId;

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long contributorId;

    @NonNull
    String login;
    @NonNull
    String avatarUrl;

    @NonNull
    Integer rank;
    @Enumerated(EnumType.STRING)
    @NonNull
    UserRankCategory rankCategory;
    @NonNull
    Integer contributionCount;
    @NonNull
    Integer contributionCountRank;
    @NonNull
    Integer rewardCount;
    @NonNull
    BigDecimal totalEarnedUsd;
    @NonNull
    Integer totalEarnedUsdRank;

    @Embeddable
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID ecosystemId;
        Long contributorId;
    }
}
