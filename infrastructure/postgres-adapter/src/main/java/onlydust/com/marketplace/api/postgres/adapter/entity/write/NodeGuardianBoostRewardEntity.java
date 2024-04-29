package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "node_guardians_boost_rewards", schema = "public")
@IdClass(NodeGuardianBoostRewardEntity.PrimaryKey.class)
public class NodeGuardianBoostRewardEntity {

    @Id
    @Column(name = "boosted_reward_id", nullable = false, updatable = false)
    UUID boostedRewardId;

    @Id
    @Column(name = "recipient_id", nullable = false, updatable = false)
    Long recipientId;

    @Column(name = "boost_reward_id")
    UUID boostRewardId;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID boostedRewardId;
        Long recipientId;
    }
}
