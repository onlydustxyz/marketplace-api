package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import com.vladmihalcea.hibernate.type.array.EnumArrayType;
import com.vladmihalcea.hibernate.type.array.internal.AbstractArrayType;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserProfileInfoEntity;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Builder(toBuilder = true)
@Table(name = "users", schema = "iam")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(
        name = "user_role[]",
        typeClass = EnumArrayType.class,
        defaultForType = AuthenticatedUser.Role[].class,
        parameters = {
                @Parameter(
                        name = AbstractArrayType.SQL_ARRAY_TYPE,
                        value = "iam.user_role"
                )
        }
)
@Accessors(fluent = true)
public class UserViewEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    UUID id;
    Long githubUserId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "githubUserId", referencedColumnName = "id", insertable = false, updatable = false)
    GithubAccountEntity githubUser;

    @Getter(AccessLevel.NONE)
    String githubLogin;
    @Getter(AccessLevel.NONE)
    String githubAvatarUrl;

    @Column(name = "email", nullable = false)
    String githubEmail;
    @Type(type = "user_role[]")
    @Column(name = "roles", nullable = false, columnDefinition = "iam.user_role[]")
    AuthenticatedUser.Role[] roles;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "user_id", insertable = false, updatable = false)
    OnboardingEntity onboarding;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id", insertable = false, updatable = false)
    UserProfileInfoEntity profile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "githubUserId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    @NonNull AllUserViewEntity allUserView;

    @ManyToMany
    @JoinTable(
            name = "sponsors_users",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "sponsor_id")
    )
    List<SponsorViewEntity> sponsors;

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
