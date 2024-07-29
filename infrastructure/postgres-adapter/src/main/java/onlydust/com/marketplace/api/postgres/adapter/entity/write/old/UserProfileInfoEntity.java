package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.AllocatedTimeEnumEntity;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.List;
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
}
