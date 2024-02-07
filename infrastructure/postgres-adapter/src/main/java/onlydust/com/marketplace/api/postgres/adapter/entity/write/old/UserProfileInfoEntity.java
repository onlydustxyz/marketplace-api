package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.AllocatedTimeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProfileCoverEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
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
@TypeDef(name = "profile_cover", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "weekly_allocated", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
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
    @Column(name = "weekly_allocated_time", nullable = false)
    @Type(type = "weekly_allocated")
    @Enumerated(EnumType.STRING)
    AllocatedTimeEnumEntity allocatedTime;
    @Type(type = "profile_cover")
    @Column(name = "cover")
    @Enumerated(EnumType.STRING)
    ProfileCoverEnumEntity cover;
    @Type(type = "jsonb")
    @Column(name = "languages", columnDefinition = "jsonb")
    Map<String, Long> languages;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false, insertable = false)
    List<ContactInformationEntity> contactInformations;

    @Column(name = "first_name")
    String firstName;
    @Column(name = "last_name")
    String lastName;
}
