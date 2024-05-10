package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.UserProfileContributingStatus;
import onlydust.com.marketplace.api.contract.model.UserProfileLanguagePageItem;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class UserProfileLanguagePageItemEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID language_id;

    @NonNull Integer rank;
    @NonNull String contributingStatus;
    @NonNull Integer contributedProjectCount;
    @NonNull Integer contributionCount;
    @NonNull Integer rewardCount;
    @NonNull BigDecimal totalEarnedUsd;

    @ManyToOne
    @JoinColumn(insertable = false, updatable = false)
    @NonNull LanguageViewEntity language;

    public UserProfileLanguagePageItem toDto() {
        return new UserProfileLanguagePageItem()
                .rank(rank)
                .contributingStatus(UserProfileContributingStatus.valueOf(contributingStatus))
                .contributedProjectCount(contributedProjectCount)
                .contributionCount(contributionCount)
                .rewardCount(rewardCount)
                .totalEarnedUsd(totalEarnedUsd)
                .projects(List.of()) // TODO
                .language(language.toDto());
    }
}
