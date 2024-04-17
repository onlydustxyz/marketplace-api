package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.AllocatedTimeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProfileCoverEnumEntity;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "user_profile_info", schema = "public")
public class UserProfileInfoEntity {

    @Id
    @Column(name = "id")
    UUID id;
    @Column(name = "bio")
    String bio;
    @Column(name = "location")
    String location;
    @Column(name = "website")
    String website;
    @Column(name = "looking_for_a_job", nullable = false)
    Boolean isLookingForAJob;
    @Column(name = "avatar_url")
    String avatarUrl;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "weekly_allocated", name = "weekly_allocated_time", nullable = false)
    AllocatedTimeEnumEntity allocatedTime;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "profile_cover")
    ProfileCoverEnumEntity cover;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "languages", columnDefinition = "jsonb")
    Map<String, Long> languages;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false, insertable = false)
    List<ContactInformationEntity> contactInformations;

    @Column(name = "first_name")
    String firstName;
    @Column(name = "last_name")
    String lastName;
}
