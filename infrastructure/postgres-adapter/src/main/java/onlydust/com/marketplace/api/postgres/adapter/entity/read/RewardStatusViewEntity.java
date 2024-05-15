package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@Value
@Table(name = "reward_statuses", schema = "accounting")
@NoArgsConstructor(force = true)
@Immutable
public class RewardStatusViewEntity {
    @Id
    @Getter(AccessLevel.NONE)
    @NonNull
    UUID rewardId;

    @Getter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "reward_status")
    @NonNull
    RewardStatus.Input status;

    public RewardStatus.Input toDomain() {
        return status;
    }
}
