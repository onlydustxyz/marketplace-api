package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.Data;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.project.domain.view.BillingProfileLinkView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@TypeDef(name = "billing_profile_role", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "billing_profile_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "verification_status", typeClass = PostgreSQLEnumType.class)
public class BillingProfileLinkViewEntity {
    @Id
    UUID id;
    @Column(name = "role", nullable = false)
    @Type(type = "billing_profile_role")
    @Enumerated(EnumType.STRING)
    BillingProfileUserEntity.Role role;
    @Type(type = "billing_profile_type")
    @Enumerated(EnumType.STRING)
    BillingProfileEntity.Type type;
    @Type(type = "verification_status")
    @Enumerated(EnumType.STRING)
    VerificationStatusEntity verificationStatus;


    public BillingProfileLinkView toDomain() {
        return BillingProfileLinkView.builder()
                .id(this.id)
                .type(switch (this.type) {
                    case INDIVIDUAL -> BillingProfileLinkView.Type.INDIVIDUAL;
                    case COMPANY -> BillingProfileLinkView.Type.COMPANY;
                    case SELF_EMPLOYED -> BillingProfileLinkView.Type.SELF_EMPLOYED;
                })
                .role(switch (this.role) {
                    case ADMIN -> BillingProfileLinkView.Role.ADMIN;
                    case MEMBER -> BillingProfileLinkView.Role.MEMBER;
                })
                .verificationStatus(switch (this.verificationStatus) {
                    case NOT_STARTED -> BillingProfileLinkView.VerificationStatus.NOT_STARTED;
                    case STARTED -> BillingProfileLinkView.VerificationStatus.STARTED;
                    case UNDER_REVIEW -> BillingProfileLinkView.VerificationStatus.UNDER_REVIEW;
                    case VERIFIED -> BillingProfileLinkView.VerificationStatus.VERIFIED;
                    case REJECTED -> BillingProfileLinkView.VerificationStatus.REJECTED;
                    case CLOSED -> BillingProfileLinkView.VerificationStatus.CLOSED;
                })
                .hasValidPayoutMethods(false)
                .hasValidVerificationStatus(false)
                .build();
    }
}
