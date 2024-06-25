package onlydust.com.marketplace.api.read.entities.user;


import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "users", schema = "iam")
@Accessors(fluent = true)
@Immutable
public class UserReadEntity implements Serializable {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NonNull
    ZonedDateTime lastSeenAt;

    @Column(name = "created_at")
    @NonNull
    Date createdAt;

    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(
                    name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "iam.user_role"
            )
    )
    @Column(nullable = false, columnDefinition = "iam.user_role[]")
    AuthenticatedUser.Role[] roles;
}
