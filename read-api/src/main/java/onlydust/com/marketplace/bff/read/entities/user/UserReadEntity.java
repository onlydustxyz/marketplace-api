package onlydust.com.marketplace.bff.read.entities.user;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Table(name = "users", schema = "iam")
@Accessors(fluent = true)
@Immutable
public class UserReadEntity implements Serializable {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;

    @NonNull ZonedDateTime lastSeenAt;
    
    @Column(name = "created_at")
    @NonNull Date createdAt;
}
