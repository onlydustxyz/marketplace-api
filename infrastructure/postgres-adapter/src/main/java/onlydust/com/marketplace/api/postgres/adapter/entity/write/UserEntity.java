package onlydust.com.marketplace.api.postgres.adapter.entity.write;


import com.vladmihalcea.hibernate.type.array.EnumArrayType;
import com.vladmihalcea.hibernate.type.array.internal.AbstractArrayType;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.UserRole;
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
@EqualsAndHashCode
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
    UUID id;
    Long githubUserId;
    String githubLogin;
    String githubAvatarUrl;
    @Column(name = "email", nullable = false)
    String githubEmail;
    @Type(type = "user_role[]")
    @Column(nullable = false, columnDefinition = "iam.user_role[]")
    UserRole[] roles;
    private Date lastSeenAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;
}
