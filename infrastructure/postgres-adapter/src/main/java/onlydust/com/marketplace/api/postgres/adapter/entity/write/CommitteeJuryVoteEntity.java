package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "committee_jury_votes", schema = "public")
@IdClass(CommitteeJuryVoteEntity.PrimaryKey.class)
public class CommitteeJuryVoteEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID projectId;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID criteriaId;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID committeeId;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID userId;
    Integer score;


    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID committeeId;
        UUID criteriaId;
        UUID userId;
    }
}
