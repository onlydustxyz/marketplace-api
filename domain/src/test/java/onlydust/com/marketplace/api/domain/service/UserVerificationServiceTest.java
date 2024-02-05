package onlydust.com.marketplace.api.domain.service;

import onlydust.com.marketplace.api.domain.job.OutboxSkippingException;
import onlydust.com.marketplace.api.domain.model.BillingProfileType;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.model.VerificationStatus;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.domain.port.output.BillingProfileStoragePort;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.Mockito.*;

public class UserVerificationServiceTest {
    private UserVerificationService userVerificationService;
    private BillingProfileStoragePort billingProfileStoragePort;
    private OutboxPort outboxPort;
    private Function<Event, BillingProfileUpdated> billingProfileExternalMapper;

    @BeforeEach
    void setUp() {
        billingProfileStoragePort = mock(BillingProfileStoragePort.class);
        outboxPort = mock(OutboxPort.class);
        billingProfileExternalMapper = mock(Function.class);
        userVerificationService = new UserVerificationService(outboxPort, billingProfileExternalMapper, billingProfileStoragePort);
    }

    @Test
    void should_update_existing_company_profile() {
        // Given
        final UUID billingProfileId = UUID.randomUUID();
        final BillingProfileUpdated event = BillingProfileUpdated.builder()
                .verificationStatus(VerificationStatus.REJECTED)
                .type(BillingProfileType.COMPANY)
                .billingProfileId(billingProfileId)
                .build();
        final Event eventStub = mock(Event.class);

        // When
        when(billingProfileExternalMapper.apply(eventStub))
                .thenReturn(event);
        when(billingProfileStoragePort.findCompanyProfileById(billingProfileId))
                .thenReturn(Optional.of(CompanyBillingProfile.builder().id(billingProfileId).userId(UUID.randomUUID()).status(VerificationStatus.STARTED).build()));
        userVerificationService.process(eventStub);

        // Then
        final ArgumentCaptor<CompanyBillingProfile> companyBillingProfileArgumentCaptor = ArgumentCaptor.forClass(CompanyBillingProfile.class);
        verify(billingProfileStoragePort, times(1)).saveCompanyProfile(companyBillingProfileArgumentCaptor.capture());
        Assertions.assertEquals(event.getVerificationStatus(), companyBillingProfileArgumentCaptor.getValue().getStatus());
    }

    @Test
    void should_raise_exception_to_skip_unknown_external_id_for_company() {
        // Given
        final BillingProfileUpdated event = BillingProfileUpdated.builder()
                .verificationStatus(VerificationStatus.REJECTED)
                .type(BillingProfileType.COMPANY)
                .billingProfileId(UUID.randomUUID())
                .build();
        final Event eventStub = mock(Event.class);

        // When
        when(billingProfileExternalMapper.apply(eventStub))
                .thenReturn(event);
        when(billingProfileStoragePort.findCompanyProfileById(UUID.randomUUID())).thenReturn(Optional.empty());
        Exception exception = null;
        try {
            userVerificationService.process(eventStub);
        } catch (Exception e) {
            exception = e;
        }

        // Then
        Assertions.assertTrue(exception instanceof OutboxSkippingException);
    }

    @Test
    void should_update_existing_individual_profile() {
        // Given
        final UUID billingProfileId = UUID.randomUUID();
        final BillingProfileUpdated event = BillingProfileUpdated.builder()
                .verificationStatus(VerificationStatus.NOT_STARTED)
                .type(BillingProfileType.INDIVIDUAL)
                .billingProfileId(billingProfileId)
                .build();
        final Event eventStub = mock(Event.class);

        // When
        when(billingProfileExternalMapper.apply(eventStub))
                .thenReturn(event);
        when(billingProfileStoragePort.findIndividualProfileById(billingProfileId))
                .thenReturn(Optional.of(IndividualBillingProfile.builder().id(billingProfileId).userId(UUID.randomUUID()).status(VerificationStatus.INVALIDATED).build()));
        userVerificationService.process(eventStub);

        // Then
        final ArgumentCaptor<IndividualBillingProfile> individualBillingProfileArgumentCaptor = ArgumentCaptor.forClass(IndividualBillingProfile.class);
        verify(billingProfileStoragePort, times(1)).saveIndividualProfile(individualBillingProfileArgumentCaptor.capture());
        Assertions.assertEquals(event.getVerificationStatus(), individualBillingProfileArgumentCaptor.getValue().getStatus());
    }

    @Test
    void should_raise_exception_to_skip_unknown_external_id_for_individual() {
        // Given
        final BillingProfileUpdated event = BillingProfileUpdated.builder()
                .verificationStatus(VerificationStatus.REJECTED)
                .type(BillingProfileType.INDIVIDUAL)
                .billingProfileId(UUID.randomUUID())
                .build();
        final Event eventStub = mock(Event.class);

        // When
        when(billingProfileExternalMapper.apply(eventStub))
                .thenReturn(event);
        when(billingProfileStoragePort.findIndividualProfileById(UUID.randomUUID())).thenReturn(Optional.empty());
        Exception exception = null;
        try {
            userVerificationService.process(eventStub);
        } catch (Exception e) {
            exception = e;
        }

        // Then
        Assertions.assertTrue(exception instanceof OutboxSkippingException);
    }


}
