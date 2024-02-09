package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.job.OutboxSkippingException;
import onlydust.com.marketplace.api.domain.model.BillingProfileType;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.model.VerificationStatus;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.domain.port.input.AccountingUserObserverPort;
import onlydust.com.marketplace.api.domain.port.output.BillingProfileStoragePort;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;
import onlydust.com.marketplace.api.domain.port.output.UserVerificationStoragePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserVerificationServiceTest {
    private UserVerificationService userVerificationService;
    private BillingProfileStoragePort billingProfileStoragePort;
    private OutboxPort outboxPort;
    private Function<Event, BillingProfileUpdated> billingProfileExternalMapper;
    private AccountingUserObserverPort accountingUserObserverPort;
    private UserVerificationStoragePort userVerificationStoragePort;
    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        billingProfileStoragePort = mock(BillingProfileStoragePort.class);
        outboxPort = mock(OutboxPort.class);
        billingProfileExternalMapper = mock(Function.class);
        accountingUserObserverPort = mock(AccountingUserObserverPort.class);
        userVerificationStoragePort = mock(UserVerificationStoragePort.class);
        userVerificationService = new UserVerificationService(outboxPort, billingProfileExternalMapper, billingProfileStoragePort, userVerificationStoragePort, accountingUserObserverPort);
    }

    @Test
    void should_update_existing_company_profile_with_verification_data() {
        // Given
        final UUID billingProfileId = UUID.randomUUID();
        final BillingProfileUpdated event = BillingProfileUpdated.builder()
                .verificationStatus(VerificationStatus.REJECTED)
                .type(BillingProfileType.COMPANY)
                .billingProfileId(billingProfileId)
                .build();
        final Event eventStub = mock(Event.class);
        final UUID userId = UUID.randomUUID();
        final CompanyBillingProfile initialCompanyBillingProfile =
                CompanyBillingProfile.builder().id(billingProfileId).userId(userId).status(VerificationStatus.STARTED).build();
        final CompanyBillingProfile companyBillingProfileWithNewStatus = initialCompanyBillingProfile.toBuilder().status(VerificationStatus.REJECTED).build();
        final CompanyBillingProfile updatedCompanyBillingProfile = initialCompanyBillingProfile.toBuilder()
                .id(billingProfileId)
                .userId(userId)
                .status(VerificationStatus.REJECTED)
                .address(faker.address().fullAddress())
                .country(faker.address().country())
                .name(faker.rickAndMorty().character())
                .euVATNumber(faker.hacker().abbreviation())
                .subjectToEuropeVAT(true)
                .registrationDate(new Date())
                .registrationNumber(faker.harryPotter().character())
                .usEntity(false)
                .build();

        // When
        when(billingProfileExternalMapper.apply(eventStub))
                .thenReturn(event);
        when(billingProfileStoragePort.findCompanyProfileById(billingProfileId))
                .thenReturn(Optional.of(initialCompanyBillingProfile));
        when(userVerificationStoragePort.updateCompanyVerification(companyBillingProfileWithNewStatus))
                .thenReturn(updatedCompanyBillingProfile);
        userVerificationService.process(eventStub);

        // Then
        final ArgumentCaptor<CompanyBillingProfile> companyBillingProfileArgumentCaptor = ArgumentCaptor.forClass(CompanyBillingProfile.class);
        verify(userVerificationStoragePort, times(1)).updateCompanyVerification(companyBillingProfileArgumentCaptor.capture());
        verify(billingProfileStoragePort, times(1)).saveCompanyProfile(companyBillingProfileArgumentCaptor.capture());
        assertEquals(companyBillingProfileWithNewStatus, companyBillingProfileArgumentCaptor.getAllValues().get(0));
        assertEquals(updatedCompanyBillingProfile, companyBillingProfileArgumentCaptor.getAllValues().get(1));
        verify(accountingUserObserverPort).onBillingProfileUpdated(event);
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
        verify(accountingUserObserverPort, never()).onBillingProfileUpdated(any());
        Assertions.assertTrue(exception instanceof OutboxSkippingException);
    }

    @Test
    void should_update_existing_individual_profile_with_verification_data() {
        // Given
        final UUID billingProfileId = UUID.randomUUID();
        final BillingProfileUpdated event = BillingProfileUpdated.builder()
                .verificationStatus(VerificationStatus.NOT_STARTED)
                .type(BillingProfileType.INDIVIDUAL)
                .billingProfileId(billingProfileId)
                .build();
        final Event eventStub = mock(Event.class);
        final IndividualBillingProfile initialIndividualBillingProfile =
                IndividualBillingProfile.builder().id(billingProfileId).userId(UUID.randomUUID()).status(VerificationStatus.INVALIDATED).build();
        final IndividualBillingProfile individualBillingProfileWithStatus =
                initialIndividualBillingProfile.toBuilder().status(VerificationStatus.NOT_STARTED).build();
        final IndividualBillingProfile updatedIndividualBillingProfile = individualBillingProfileWithStatus.toBuilder()
                .country(faker.address().country())
                .address(faker.address().fullAddress())
                .birthdate(new Date())
                .idDocumentNumber(faker.idNumber().valid())
                .idDocumentType(IndividualBillingProfile.IdDocumentTypeEnum.PASSPORT)
                .usCitizen(false)
                .validUntil(new Date())
                .build();

        // When
        when(billingProfileExternalMapper.apply(eventStub))
                .thenReturn(event);
        when(billingProfileStoragePort.findIndividualProfileById(billingProfileId))
                .thenReturn(Optional.of(initialIndividualBillingProfile));
        when(userVerificationStoragePort.updateIndividualVerification(individualBillingProfileWithStatus))
                .thenReturn(updatedIndividualBillingProfile);
        userVerificationService.process(eventStub);

        // Then
        final ArgumentCaptor<IndividualBillingProfile> individualBillingProfileArgumentCaptor = ArgumentCaptor.forClass(IndividualBillingProfile.class);
        verify(userVerificationStoragePort, times(1)).updateIndividualVerification(individualBillingProfileArgumentCaptor.capture());
        verify(billingProfileStoragePort, times(1)).saveIndividualProfile(individualBillingProfileArgumentCaptor.capture());
        assertEquals(individualBillingProfileWithStatus, individualBillingProfileArgumentCaptor.getAllValues().get(0));
        assertEquals(updatedIndividualBillingProfile, individualBillingProfileArgumentCaptor.getAllValues().get(1));
        verify(accountingUserObserverPort).onBillingProfileUpdated(event);
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
        verify(accountingUserObserverPort, never()).onBillingProfileUpdated(any());
    }
}
