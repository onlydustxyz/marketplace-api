package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "ecosystem_leads", schema = "public")
@IdClass(EcosystemLeadEntity.PrimaryKey.class)
public class EcosystemLeadEntity {
    @Id
    @Column(nullable = false, updatable = false)
    UUID userId;

    @Id
    @Column(nullable = false, updatable = false)
    UUID ecosystemId;

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID ecosystemId;
    }
}
