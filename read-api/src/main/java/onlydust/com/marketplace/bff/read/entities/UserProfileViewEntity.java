package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.ContactInformation;
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
public class UserProfileViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;

    String location;
    String bio;
    String website;

    @OneToMany(mappedBy = "userId")
    @Getter(AccessLevel.NONE)
    List<ContactInformationViewEntity> contacts;

    Optional<List<ContactInformation>> publicContacts() {
        return Optional.ofNullable(contacts).map(c -> c.stream().filter(ContactInformationViewEntity::isPublic).map(ContactInformationViewEntity::toDto).toList());
    }
}
