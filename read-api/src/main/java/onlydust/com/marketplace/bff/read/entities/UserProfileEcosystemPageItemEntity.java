package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.UserProfileContributingStatus;
import onlydust.com.marketplace.api.contract.model.UserProfileEcosystemPageItem;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.EcosystemViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLinkViewEntity;
import onlydust.com.marketplace.bff.read.mapper.ProjectMapper;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.bff.read.mapper.EcosystemMapper.map;

@Entity
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class UserProfileEcosystemPageItemEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID ecosystem_id;

    @NonNull Integer rank;
    @NonNull String contributingStatus;
    @NonNull Integer contributionCount;
    @NonNull Integer rewardCount;
    @NonNull BigDecimal totalEarnedUsd;

    @ManyToOne
    @JoinColumn(insertable = false, updatable = false)
    @NonNull EcosystemViewEntity ecosystem;

    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull List<ProjectLinkViewEntity> projects;

    public UserProfileEcosystemPageItem toDto() {
        return new UserProfileEcosystemPageItem()
                .rank(rank)
                .contributingStatus(UserProfileContributingStatus.valueOf(contributingStatus))
                .contributedProjectCount(projects.size())
                .contributionCount(contributionCount)
                .rewardCount(rewardCount)
                .totalEarnedUsd(totalEarnedUsd)
                .projects(projects.stream().map(ProjectMapper::map).toList())
                .ecosystem(map(ecosystem));
    }
}
