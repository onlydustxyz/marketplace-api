package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountViewEntity;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "users", schema = "iam")
@EntityListeners(AuditingEntityListener.class)
@Accessors(fluent = true)
@Immutable
public class UserViewEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    UUID id;
    Long githubUserId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "githubUserId", referencedColumnName = "id", insertable = false, updatable = false)
    GithubAccountViewEntity githubUser;

    @Column(nullable = false)
    String email;

    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(
                    name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "iam.user_role"
            )
    )
    @Column(name = "roles", nullable = false, columnDefinition = "iam.user_role[]")
    AuthenticatedUser.Role[] roles;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "user_id", insertable = false, updatable = false)
    OnboardingViewEntity onboarding;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id", insertable = false, updatable = false)
    UserProfileInfoViewEntity profile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "githubUserId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    @NonNull
    AllUserViewEntity allUserView;

    @NonNull
    ZonedDateTime lastSeenAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Date updatedAt;

    public String login() {
        return allUserView.login();
    }

    public String avatarUrl() {
        return allUserView.avatarUrl();
    }
}
