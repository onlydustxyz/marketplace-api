package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.JourneyCompletionResponse;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "users", schema = "iam")
public class JourneyCompletionEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID id;

    @Formula("""
            exists(select 1
                   from accounting.billing_profiles_users bpu
                            join accounting.billing_profiles bp
                                 on bpu.billing_profile_id = bp.id and bp.verification_status = 'VERIFIED'
                   where bpu.user_id = id)
            """)
    boolean billingProfileVerified;

    @Formula("""
            exists(select 1
            from accounting.billing_profiles_users bpu
                     join accounting.billing_profiles bp
                          on bpu.billing_profile_id = bp.id and bp.type in ('COMPANY', 'SELF_EMPLOYED') and
                             bp.verification_status = 'VERIFIED'
            where bpu.user_id = id)
            """)
    boolean companyBillingProfileVerified;

    @Formula("""
            exists(select 1
                   from user_profile_info upi
                   where upi.id = id
                     and length(coalesce(upi.bio, '')) > 0)
            """)
    boolean descriptionUpdated;

    @Formula("""
            exists(select 1
                   from contact_informations ci
                   where ci.user_id = id
                     and ci.channel = 'TELEGRAM'
                     and length(ci.contact) > 0)
            """)
    boolean telegramAdded;

    @Formula("""
            exists(select 1
                   from iam.users u
                            join rewards r on r.recipient_id = u.github_user_id
                   where u.id = id)
            """)
    boolean rewardReceived;

    @Formula("""
            exists(select 1
                   from iam.users u
                            join rewards r on r.recipient_id = u.github_user_id
                            join accounting.reward_statuses rs on rs.reward_id = r.id and rs.status > 'PENDING_REQUEST'
                   where u.id = id)
            """)
    boolean rewardClaimed;


    public JourneyCompletionResponse toResponse() {
        final var allItems = List.of(billingProfileVerified, companyBillingProfileVerified, descriptionUpdated, telegramAdded, rewardReceived, rewardClaimed);
        final var completedItems = allItems.stream().filter(Boolean::booleanValue).count();

        return new JourneyCompletionResponse()
                .completion(BigDecimal.valueOf(completedItems * 100 / allItems.size()))
                .completed(completedItems == allItems.size())
                .billingProfileVerified(billingProfileVerified)
                .companyBillingProfileVerified(companyBillingProfileVerified)
                .descriptionUpdated(descriptionUpdated)
                .telegramAdded(telegramAdded)
                .rewardReceived(rewardReceived)
                .rewardClaimed(rewardClaimed)
                ;
    }
}
