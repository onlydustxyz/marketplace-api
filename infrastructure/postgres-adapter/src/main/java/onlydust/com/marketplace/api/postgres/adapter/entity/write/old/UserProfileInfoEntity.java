package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.AllocatedTimeEnumEntity;
import onlydust.com.marketplace.project.domain.model.UserProfile;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "user_profile_info", schema = "public")
public class UserProfileInfoEntity {

    @Id
    UUID id;
    String bio;
    String location;
    String website;
    @Column(name = "looking_for_a_job")
    Boolean isLookingForAJob;
    String avatarUrl;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    AllocatedTimeEnumEntity weeklyAllocatedTime;
    String firstName;
    String lastName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false, insertable = false)
    List<ContactInformationEntity> contactInformations;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "user_joining_goals")
    UserProfile.JoiningGoal joiningGoal;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "user_joining_reasons")
    UserProfile.JoiningReason joiningReason;

    @Type(value = UUIDArrayType.class)
    @Column(nullable = false, columnDefinition = "uuid[]")
    UUID[] preferredLanguageIds;

    @Type(value = UUIDArrayType.class)
    @Column(nullable = false, columnDefinition = "uuid[]")
    UUID[] preferredCategoryIds;


    public UserProfile toDomain() {
        return UserProfile.builder()
                .bio(bio)
                .location(location)
                .website(website)
                .isLookingForAJob(isLookingForAJob)
                .avatarUrl(avatarUrl)
                    .allocatedTimeToContribute(isNull(weeklyAllocatedTime) ? null : weeklyAllocatedTime.toDomain())
                .firstName(firstName)
                .lastName(lastName)
                .contacts(contactInformations.stream().map(ContactInformationEntity::toDomain).toList())
                .build();
    }
}
