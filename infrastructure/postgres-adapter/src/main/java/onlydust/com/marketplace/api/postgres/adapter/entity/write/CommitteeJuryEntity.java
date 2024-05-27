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
@Table(name = "committee_juries", schema = "public")
@IdClass(CommitteeJuryEntity.PrimaryKey.class)
public class CommitteeJuryEntity {

    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID committeeId;
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID userId;


    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID committeeId;
        UUID userId;
    }
}
