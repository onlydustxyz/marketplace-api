package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.write;

import com.vladmihalcea.hibernate.type.array.EnumArrayType;
import com.vladmihalcea.hibernate.type.array.internal.AbstractArrayType;
import lombok.*;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
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
@TypeDef(
        name = "backoffice_user_role[]",
        typeClass = EnumArrayType.class,
        defaultForType = BackofficeUser.Role[].class,
        parameters = {
                @Parameter(
                        name = AbstractArrayType.SQL_ARRAY_TYPE,
                        value = "iam.backoffice_user_role"
                )
        }
)
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
    @Type(type = "backoffice_user_role[]")
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
        return new BackofficeUser(BackofficeUser.Id.of(id), email, name, Set.of(roles), avatarUrl);
    }
}
