package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "user_profile_info", schema = "public")
@Value
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
    @Column(name = "avatar_url")
    String avatarUrl;
    @Column(name = "first_name")
    String firstName;
    @Column(name = "last_name")
    String lastName;

    @OneToMany(mappedBy = "userId")
    @Getter(AccessLevel.NONE)
    List<ContactInformationViewEntity> contacts;

    public Optional<List<ContactInformationViewEntity>> publicContacts() {
        return Optional.ofNullable(contacts).map(c -> c.stream().filter(ContactInformationViewEntity::isPublic).toList());
    }
}
