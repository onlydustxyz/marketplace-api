package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder(toBuilder = true)
@Table(name = "billing_profiles_user_invitations", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
@IdClass(BillingProfileUserInvitationEntity.PrimaryKey.class)
public class BillingProfileUserInvitationEntity {

    @Id
    UUID billingProfileId;
    @Id
    Long githubUserId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "billing_profile_role")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    BillingProfileUserEntity.Role role;

    Date invitedAt;
    UUID invitedBy;
    @Builder.Default
    Boolean accepted = false;

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID billingProfileId;
        Long githubUserId;
    }
}
