package onlydust.com.marketplace.api.postgres.adapter.entity.write;


import com.vladmihalcea.hibernate.type.array.EnumArrayType;
import com.vladmihalcea.hibernate.type.array.internal.AbstractArrayType;
import lombok.*;
import onlydust.com.marketplace.api.domain.model.UserRole;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(toBuilder = true)
@Table(name = "users", schema = "iam")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(
        name = "user_role[]",
        typeClass = EnumArrayType.class,
        defaultForType = UserRole[].class,
        parameters = {
                @Parameter(
                        name = AbstractArrayType.SQL_ARRAY_TYPE,
                        value = "iam.user_role"
                )
        }
)
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false)
    UUID id;
    @Column(name = "github_user_id", nullable = false)
    Long githubUserId;
    @Column(name = "github_login", nullable = false)
    String githubLogin;
    @Column(name = "github_avatar_url", nullable = false)
    String githubAvatarUrl;
    @Column(name = "email", nullable = false)
    String email;
    @Type(type = "user_role[]")
    @Column(name = "roles", nullable = false, columnDefinition = "iam.user_role[]")
    UserRole[] roles;
    @Column(name = "last_seen_at", nullable = false)
    private Date lastSeenAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;
}
