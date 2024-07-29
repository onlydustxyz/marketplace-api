package onlydust.com.marketplace.api.postgres.adapter.entity.write;


import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.ShortContributorView;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.user.domain.model.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Arrays;
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
public class UserEntity {

    @Id
    UUID id;
    Long githubUserId;
    String githubLogin;
    String githubAvatarUrl;
    @Column(name = "email", nullable = false)
    String githubEmail;

    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(
                    name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "iam.user_role"
            )
    )
    @Column(nullable = false, columnDefinition = "iam.user_role[]")
    AuthenticatedUser.Role[] roles;
    private Date lastSeenAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    @SuppressWarnings("unused") // it is used in the repository's native insert query
    public String rolesAsPostgresArray() {
        return "{" + String.join(",", Arrays.stream(roles).map(Enum::name).toList()) + "}";
    }

    public ShortContributorView toShortContributorView() {
        return new ShortContributorView(GithubUserId.of(githubUserId), githubLogin, githubAvatarUrl, UserId.of(id), githubEmail);
    }

    public User toUser() {
        return new User(onlydust.com.marketplace.user.domain.model.UserId.of(id), githubEmail, githubLogin);
    }
}
