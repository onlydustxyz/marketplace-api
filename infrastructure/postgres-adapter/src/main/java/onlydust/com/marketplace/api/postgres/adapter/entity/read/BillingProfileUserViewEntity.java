package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Table(name = "billing_profiles_users", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
@IdClass(BillingProfileUserViewEntity.PrimaryKey.class)
@Immutable
public class BillingProfileUserViewEntity {

    @Id
    @Column(name = "billing_profile_id", nullable = false, updatable = false)
    UUID billingProfileId;

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    UUID userId;

    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    @OneToOne(fetch = FetchType.LAZY)
    UserViewEntity user;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "billing_profile_role")
    BillingProfile.User.Role role;

    ZonedDateTime joinedAt;

    @EqualsAndHashCode
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID billingProfileId;
    }
}
