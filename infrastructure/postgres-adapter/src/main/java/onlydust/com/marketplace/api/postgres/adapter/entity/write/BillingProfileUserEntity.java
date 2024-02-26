package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder(toBuilder = true)
@Table(name = "billing_profiles_users", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
@IdClass(BillingProfileUserEntity.PrimaryKey.class)
@TypeDef(name = "billing_profile_role", typeClass = PostgreSQLEnumType.class)
public class BillingProfileUserEntity {

    @Id
    @Column(name = "billing_profile_id", nullable = false, updatable = false)
    UUID billingProfileId;

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    UUID userId;

    @Column(name = "role", nullable = false)
    @Type(type = "billing_profile_type")
    @Enumerated(EnumType.STRING)
    Role role;

    Date joinedAt;
    Date invitedAt;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public enum Role {
        ADMIN, MEMBER;

        public static Role fromDomain(final BillingProfile.User.Role role) {
            return switch (role) {
                case ADMIN -> Role.ADMIN;
                case MEMBER -> Role.MEMBER;
            };
        }

        public BillingProfile.User.Role toDomain() {
            return switch (this) {
                case ADMIN -> BillingProfile.User.Role.ADMIN;
                case MEMBER -> BillingProfile.User.Role.MEMBER;
            };
        }
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID billingProfileId;
    }
}
