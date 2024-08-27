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
@Table(name = "sponsor_leads", schema = "public")
@IdClass(SponsorLeadEntity.PrimaryKey.class)
public class SponsorLeadEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    UUID userId;
    @Id
    @Column(nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    UUID sponsorId;

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID sponsorId;
    }
}
