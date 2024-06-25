package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "user_profile_info", schema = "public")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class UserProfileInfoViewEntity {
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

    @OneToMany(mappedBy = "userId")
    @Getter(AccessLevel.NONE)
    List<ContactInformationViewEntity> contacts;

    public Optional<List<ContactInformationViewEntity>> publicContacts() {
        return Optional.ofNullable(contacts).map(c -> c.stream().filter(ContactInformationViewEntity::isPublic).toList());
    }
}
