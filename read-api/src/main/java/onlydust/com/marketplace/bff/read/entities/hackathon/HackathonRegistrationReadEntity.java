package onlydust.com.marketplace.bff.read.entities.hackathon;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.bff.read.entities.user.AllUserReadEntity;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Accessors(fluent = true)
@Table(name = "hackathon_registrations", schema = "public")
@IdClass(HackathonRegistrationReadEntity.PrimaryKey.class)
public class HackathonRegistrationReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID hackathonId;

    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID userId;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false)
    @NonNull AllUserReadEntity user;

    @NonNull ZonedDateTime techCreatedAt;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        @NonNull UUID hackathonId;
        @NonNull UUID userId;
    }
}
