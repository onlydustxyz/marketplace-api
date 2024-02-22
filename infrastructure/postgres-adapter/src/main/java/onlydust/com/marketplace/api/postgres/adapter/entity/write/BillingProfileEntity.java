package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder(toBuilder = true)
@Table(name = "billing_profiles", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "billing_profile_type", typeClass = PostgreSQLEnumType.class)
public class BillingProfileEntity {
    @Id
    UUID id;
    String name;
    @org.hibernate.annotations.Type(type = "billing_profile_type")
    @Enumerated(EnumType.STRING)
    Type type;
    Date invoiceMandateAcceptedAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "billingProfileId")
    Set<BillingProfileUserEntity> users;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public enum Type {
        INDIVIDUAL,
        COMPANY,
        SELF_EMPLOYED
    }

    public static BillingProfileEntity fromDomain(final BillingProfile billingProfile, final UserId ownerId) {
        return BillingProfileEntity.builder()
                .id(billingProfile.id().value())
                .name(billingProfile.name())
                .type(switch (billingProfile.type()) {
                    case COMPANY -> Type.COMPANY;
                    case SELF_EMPLOYED -> Type.SELF_EMPLOYED;
                    case INDIVIDUAL -> Type.INDIVIDUAL;
                })
                .users(isNull(ownerId) ? Set.of() : Set.of(BillingProfileUserEntity.builder()
                        .billingProfileId(billingProfile.id().value())
                        .userId(ownerId.value())
                        .role(BillingProfileUserEntity.Role.ADMIN)
                        .build())
                )
                .build();
    }


    public BillingProfileView toView() {
        return BillingProfileView.builder()
                .id(BillingProfile.Id.of(this.id))
                .type(switch (this.type) {
                    case INDIVIDUAL -> BillingProfile.Type.INDIVIDUAL;
                    case COMPANY -> BillingProfile.Type.COMPANY;
                    case SELF_EMPLOYED -> BillingProfile.Type.SELF_EMPLOYED;
                })
                .build();
    }
}
