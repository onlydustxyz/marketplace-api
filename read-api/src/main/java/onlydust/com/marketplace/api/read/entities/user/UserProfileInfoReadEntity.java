package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.AllocatedTimeEnumEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

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

    @OneToMany(mappedBy = "userId")
    @Getter(AccessLevel.NONE)
    List<ContactInformationReadEntity> contacts;

    @Column(name = "looking_for_a_job")
    Boolean isLookingForAJob;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Getter(AccessLevel.NONE)
    AllocatedTimeEnumEntity weeklyAllocatedTime;

    public Optional<AllocatedTimeEnumEntity> weeklyAllocatedTime() {
        return Optional.ofNullable(weeklyAllocatedTime);
    }

    public Optional<List<ContactInformationReadEntity>> publicContacts() {
        return Optional.ofNullable(contacts).map(c -> c.stream().filter(ContactInformationReadEntity::getIsPublic).toList());
    }

    public List<ContactInformationReadEntity> allContacts() {
        return contacts == null ? List.of() : contacts;
    }
}
