package onlydust.com.marketplace.api.read.entities.hackathon;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Table(name = "hackathon_registrations", schema = "public")
@IdClass(HackathonRegistrationReadEntity.PrimaryKey.class)
@Immutable
public class HackathonRegistrationReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID hackathonId;

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID userId;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false)
    @NonNull
    AllUserReadEntity user;

    @NonNull
    ZonedDateTime techCreatedAt;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        @NonNull
        UUID hackathonId;
        @NonNull
        UUID userId;
    }
}
