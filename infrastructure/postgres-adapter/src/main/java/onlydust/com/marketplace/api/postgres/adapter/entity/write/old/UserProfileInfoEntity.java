package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.AllocatedTimeEnumEntity;
import onlydust.com.marketplace.project.domain.model.UserProfile;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.api.postgres.adapter.mapper.UserMapper.mapAllocatedTimeToEntity;
import static onlydust.com.marketplace.api.postgres.adapter.mapper.UserMapper.mapContactInformationsToEntity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(toBuilder = true)
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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "userProfileInfo")
    Set<ContactInformationEntity> contactInformations = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "user_joining_goals")
    UserProfile.JoiningGoal joiningGoal;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "user_joining_reasons")
    UserProfile.JoiningReason joiningReason;

    @Type(value = UUIDArrayType.class)
    @Column(columnDefinition = "uuid[]")
    UUID[] preferredLanguageIds;

    @Type(value = UUIDArrayType.class)
    @Column(columnDefinition = "uuid[]")
    UUID[] preferredCategoryIds;


    public UserProfileInfoEntity update(final UUID userId, UserProfile userProfile) {
        final Set<ContactInformationEntity> contactInformation = mapContactInformationsToEntity(userId, userProfile.contacts());

        final UserProfileInfoEntity userProfileInfoEntity = this.toBuilder()
                .avatarUrl(userProfile.avatarUrl())
                .bio(userProfile.bio())
                .location(userProfile.location())
                .website(userProfile.website())
                .weeklyAllocatedTime(mapAllocatedTimeToEntity(userProfile.allocatedTimeToContribute()))
                .isLookingForAJob(userProfile.isLookingForAJob())
                .lastName(userProfile.lastName())
                .firstName(userProfile.firstName())
                .id(userId)
                .joiningGoal(userProfile.joiningGoal())
                .joiningReason(userProfile.joiningReason())
                .preferredCategoryIds(isNull(userProfile.preferredCategoriesIds()) ? null : userProfile.preferredCategoriesIds().toArray(new UUID[0]))
                .preferredLanguageIds(isNull(userProfile.preferredLanguageIds()) ? null : userProfile.preferredLanguageIds().toArray(new UUID[0]))
                .build();

        userProfileInfoEntity.updateContactInfos(contactInformation);

        return userProfileInfoEntity;
    }

    private void updateContactInfos(final Set<ContactInformationEntity> contactInformationToUpdate) {

        if (nonNull(this.contactInformations)) {
            for (ContactInformationEntity contactInformation : this.contactInformations) {
                contactInformation.setUserProfileInfo(null);
            }
            this.contactInformations.clear();
        }
        if (nonNull(contactInformationToUpdate)) {
            contactInformationToUpdate.forEach(contactInformation -> contactInformation.setUserProfileInfo(this));
            this.contactInformations.addAll(contactInformationToUpdate);
        }
    }


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
