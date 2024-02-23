package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Value
@NoArgsConstructor(force = true)
@Entity
public class UserRewardViewEntity {
    @Id
    UUID id;
    Date requestedAt;
    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    ProjectEntity project;
    BigDecimal amount;
    @ManyToOne
    CurrencyEntity currency;
    BigDecimal dollarsEquivalent;
    Integer contributionCount;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    RewardStatusEntity status;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    RewardStatusDataEntity statusData;
}
