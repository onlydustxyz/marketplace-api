package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.OnboardingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.UUID;

public class AllRepositoriesIT extends AbstractPostgresIT {

    @Autowired
    OnboardingRepository onboardingRepository;

    @Test
    void should_create_onboarding() {
        // Given
        final OnboardingEntity onboardingEntity = OnboardingEntity.builder().id(UUID.randomUUID())
                .termsAndConditionsAcceptanceDate(new Date())
                .profileWizardDisplayDate(new Date())
                .build();

        // When
        onboardingRepository.save(onboardingEntity);

        // Then
        Assertions.assertEquals(1, onboardingRepository.findAll().size());
    }
}
