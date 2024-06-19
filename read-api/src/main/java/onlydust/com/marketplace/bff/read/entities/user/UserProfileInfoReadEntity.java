package onlydust.com.marketplace.bff.read.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Table(name = "user_profile_info", schema = "public")
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class UserProfileInfoReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    String location;
    String bio;
    String website;
    String avatarUrl;
    String firstName;
    String lastName;
}
