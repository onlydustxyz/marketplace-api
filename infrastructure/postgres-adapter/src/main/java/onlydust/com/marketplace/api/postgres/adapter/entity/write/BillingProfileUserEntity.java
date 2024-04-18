package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.project.domain.view.BillingProfileLinkView;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.net.URI;
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
public class BillingProfileUserEntity {

    @Id
    @Column(name = "billing_profile_id", nullable = false, updatable = false)
    UUID billingProfileId;

    @ManyToOne
    @JoinColumn(name = "billing_profile_id", referencedColumnName = "id", insertable = false, updatable = false)
    BillingProfileEntity billingProfile;

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    UUID userId;

    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    @OneToOne
    UserViewEntity user;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "billing_profile_role")
    Role role;

    Date joinedAt;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public BillingProfileView.User toView() {
        return new BillingProfileView.User(
                UserId.of(user.id()),
                GithubUserId.of(user.githubUserId()),
                user.login(),
                URI.create(user.avatarUrl()),
                user.githubEmail());
    }

    public BillingProfileLinkView toBillingProfileLinkView() {
        return BillingProfileLinkView.builder()
                .id(billingProfileId)
                .type(switch (billingProfile.type) {
                    case INDIVIDUAL -> BillingProfileLinkView.Type.INDIVIDUAL;
                    case COMPANY -> BillingProfileLinkView.Type.COMPANY;
                    case SELF_EMPLOYED -> BillingProfileLinkView.Type.SELF_EMPLOYED;
                })
                .role(switch (role) {
                    case ADMIN -> BillingProfileLinkView.Role.ADMIN;
                    case MEMBER -> BillingProfileLinkView.Role.MEMBER;
                })
                .verificationStatus(switch (billingProfile.verificationStatus) {
                    case NOT_STARTED -> BillingProfileLinkView.VerificationStatus.NOT_STARTED;
                    case STARTED -> BillingProfileLinkView.VerificationStatus.STARTED;
                    case UNDER_REVIEW -> BillingProfileLinkView.VerificationStatus.UNDER_REVIEW;
                    case VERIFIED -> BillingProfileLinkView.VerificationStatus.VERIFIED;
                    case REJECTED -> BillingProfileLinkView.VerificationStatus.REJECTED;
                    case CLOSED -> BillingProfileLinkView.VerificationStatus.CLOSED;
                })
                .missingPayoutInfo(billingProfile.stats.missingPayoutInfo())
                .missingVerification(billingProfile.stats.missingVerification())
                .build();
    }

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
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID billingProfileId;
    }
}
