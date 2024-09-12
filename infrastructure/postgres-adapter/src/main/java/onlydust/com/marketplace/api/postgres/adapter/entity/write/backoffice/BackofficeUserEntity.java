package onlydust.com.marketplace.api.postgres.adapter.entity.write.backoffice;

import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder(toBuilder = true)
@Table(name = "backoffice_users", schema = "iam")
@EntityListeners(AuditingEntityListener.class)
public class BackofficeUserEntity {
    @Id
    @Column(name = "id", nullable = false)
    UUID id;
    @Column(name = "email", nullable = false)
    String email;
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "avatarUrl", nullable = false)
    String avatarUrl;
    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(
                    name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "iam.backoffice_user_role"
            )
    )
    @Column(name = "roles", nullable = false, columnDefinition = "iam.backoffice_user_role[]")
    BackofficeUser.Role[] roles;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public static BackofficeUserEntity fromDomain(BackofficeUser user) {
        return BackofficeUserEntity.builder()
                .id(user.id().value())
                .email(user.email())
                .name(user.name())
                .roles(user.roles().toArray(BackofficeUser.Role[]::new))
                .avatarUrl(user.avatarUrl())
                .build();
    }

    public BackofficeUser toDomain() {
        return new BackofficeUser(UserId.of(id), email, name, Set.of(roles), avatarUrl);
    }
}
