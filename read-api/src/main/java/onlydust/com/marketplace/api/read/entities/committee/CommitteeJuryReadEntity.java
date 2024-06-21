package onlydust.com.marketplace.api.read.entities.committee;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Immutable
@Table(name = "committee_juries", schema = "public")
@IdClass(CommitteeJuryReadEntity.PrimaryKey.class)
@Accessors(fluent = true)
public class CommitteeJuryReadEntity {

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID committeeId;

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committeeId", insertable = false, updatable = false)
    private CommitteeReadEntity committee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false)
    private AllUserReadEntity user;

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID committeeId;
        UUID userId;
    }
}
