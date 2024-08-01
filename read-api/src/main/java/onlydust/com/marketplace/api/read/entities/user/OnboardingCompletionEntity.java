package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.OnboardingCompletionResponse;
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
public class OnboardingCompletionEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID id;

    @Formula("""
            exists(select 1
                   from accounting.billing_profiles_users bpu
                      join accounting.billing_profiles bp on bpu.billing_profile_id = bp.id
                   where bpu.user_id = id and bpu.role = 'ADMIN')
            """)
    boolean payoutInformationProvided;

    @Formula("""
            exists(select 1
                   from onboardings o
                   join global_settings gs on gs.id = 1
                   where o.user_id = id and
                   o.terms_and_conditions_acceptance_date is not null and
                   o.terms_and_conditions_acceptance_date >= gs.terms_and_conditions_latest_version_date)
            """)
    boolean termsAndConditionsAccepted;

    @Formula("""
            (select coalesce(upi.first_name is not null and upi.last_name is not null
                            and upi.location is not null and upi.bio is not null and
                            upi.website is not null, false)
            from iam.users u
                     left join user_profile_info upi on upi.id = u.id
            where u.id = id)
            """)
    boolean profileCompleted;

    @Formula("""
            (select coalesce(upi.joining_reason is not null and
                            upi.joining_goal is not null and
                            coalesce(upi.looking_for_a_job, false) and
                            upi.preferred_language_ids is not null and
                            upi.preferred_category_ids is not null and
                            cardinality(upi.preferred_language_ids) > 0 and
                            cardinality(upi.preferred_category_ids) > 0, false)
            from iam.users u
                     left join user_profile_info upi on upi.id = u.id
            where u.id = id)
            """)
    boolean projectPreferencesProvided;

    @Formula("""
            (select coalesce(o.completion_date is not null, false)
            from iam.users u
                     left join onboardings o on o.user_id = u.id
            where u.id = id)
            """)
    boolean hasCompletedOnboarding;

    @Formula("""
            (select coalesce(upi.contact_email is not null, false)
            from iam.users u
                     left join user_profile_info upi on upi.id = u.id
            where u.id = id)
            """)
    boolean verificationInformationProvided;

    public OnboardingCompletionResponse toResponse() {
        final var allItems = List.of(verificationInformationProvided,
                termsAndConditionsAccepted,
                projectPreferencesProvided,
                profileCompleted,
                payoutInformationProvided);
        final var completedItems = allItems.stream().filter(Boolean::booleanValue).count();

        return new OnboardingCompletionResponse()
                .completion(BigDecimal.valueOf(completedItems * 100 / allItems.size()))
                .completed(hasCompletedOnboarding)
                .verificationInformationProvided(verificationInformationProvided)
                .termsAndConditionsAccepted(termsAndConditionsAccepted)
                .projectPreferencesProvided(projectPreferencesProvided)
                .profileCompleted(profileCompleted)
                .payoutInformationProvided(payoutInformationProvided)
                ;
    }
}
