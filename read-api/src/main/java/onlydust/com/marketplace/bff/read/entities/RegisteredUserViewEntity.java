package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "iam")
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class RegisteredUserViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;

    @Column(name = "created_at")
    @NonNull ZonedDateTime createdAt;
    @NonNull ZonedDateTime lastSeenAt;
}
