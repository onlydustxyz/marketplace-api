package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "committee_juries", schema = "public")
@IdClass(CommitteeJuryEntity.PrimaryKey.class)
public class CommitteeJuryEntity {

    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID committeeId;

    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committeeId", insertable = false, updatable = false)
    private CommitteeEntity committee;

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID committeeId;
        UUID userId;
    }

    public static CommitteeJuryEntity fromDomain(final CommitteeEntity committee, final UUID userId) {
        return CommitteeJuryEntity.builder()
                .committeeId(committee.id)
                .userId(userId)
                .committee(committee)
                .build();
    }
}
