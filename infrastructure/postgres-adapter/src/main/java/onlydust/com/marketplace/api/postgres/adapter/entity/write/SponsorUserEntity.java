package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "sponsors_users", schema = "public")
@IdClass(SponsorUserEntity.PrimaryKey.class)
public class SponsorUserEntity {

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
