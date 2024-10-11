package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.UserId;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
public class AuthenticatedUserQueryEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    Long githubUserId;
    @NonNull
    String githubLogin;
    @NonNull
    String githubAvatarUrl;
    @NonNull
    String email;

    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(
                    name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "iam.user_role"
            )
    )
    @Column(nullable = false, columnDefinition = "iam.user_role[]")
    AuthenticatedUser.Role[] roles;

    @Type(value = UUIDArrayType.class)
    @Column(columnDefinition = "uuid[]")
    UUID[] projectLedIds;

    @JdbcTypeCode(SqlTypes.JSON)
    List<AuthenticatedUser.BillingProfileMembership> billingProfiles;


    public AuthenticatedUser toDomain() {
        return AuthenticatedUser.builder()
                .id(UserId.of(id))
                .githubUserId(githubUserId)
                .login(githubLogin)
                .avatarUrl(githubAvatarUrl)
                .email(email)
                .roles(roles == null ? List.of() : List.of(roles))
                .projectsLed(projectLedIds == null ? List.of() : List.of(projectLedIds))
                .billingProfiles(billingProfiles == null ? List.of() : billingProfiles)
                .build();
    }
}
