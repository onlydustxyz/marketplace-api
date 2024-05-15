package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@Value
@Table(name = "reward_statuses", schema = "accounting")
@Accessors(fluent = true)
@NoArgsConstructor(force = true)
public class RewardStatusEntity {
    @Id
    @Getter(AccessLevel.NONE)
    @NonNull
    UUID rewardId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "reward_status")
    @NonNull
    RewardStatus.Input status;
}
