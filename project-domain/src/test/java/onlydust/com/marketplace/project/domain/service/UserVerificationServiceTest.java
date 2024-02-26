package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.jobs.OutboxSkippingException;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.project.domain.port.input.AccountingUserObserverPort;
import onlydust.com.marketplace.project.domain.port.output.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserVerificationServiceTest {


    @Nested
    class GivenAParentOldBillingProfile {
        private UserVerificationService userVerificationService;
        private OldBillingProfileStoragePort oldBillingProfileStoragePort;
        private OutboxPort outboxPort;
        private Function<Event, BillingProfileUpdated> billingProfileExternalMapper;
        private AccountingUserObserverPort accountingUserObserverPort;
        private UserVerificationStoragePort userVerificationStoragePort;
        private NotificationPort notificationPort;
        private UserStoragePort userStoragePort;
        private WebhookPort webhookPort;
        private final Faker faker = new Faker();

        @BeforeEach
        void setUp() {
            oldBillingProfileStoragePort = mock(OldBillingProfileStoragePort.class);
            outboxPort = mock(OutboxPort.class);
            billingProfileExternalMapper = mock(Function.class);
            accountingUserObserverPort = mock(AccountingUserObserverPort.class);
            userVerificationStoragePort = mock(UserVerificationStoragePort.class);
            notificationPort = mock(NotificationPort.class);
            userStoragePort = mock(UserStoragePort.class);
            webhookPort = mock(WebhookPort.class);
            userVerificationService = new UserVerificationService(outboxPort, billingProfileExternalMapper, oldBillingProfileStoragePort,
                    userVerificationStoragePort, accountingUserObserverPort, notificationPort, userStoragePort, webhookPort);
        }

        @Test
        void should_update_existing_company_profile_with_verification_data() {
            // Given
            final UUID billingProfileId = UUID.randomUUID();
            final String reviewMessageForApplicant = faker.rickAndMorty().location();
            final String applicantId = faker.rickAndMorty().character();
            final BillingProfileUpdated event =
                    BillingProfileUpdated.builder().oldVerificationStatus(OldVerificationStatus.REJECTED)
                            .type(OldBillingProfileType.COMPANY)
                            .billingProfileId(billingProfileId)
                            .externalApplicantId(applicantId)
                            .reviewMessageForApplicant(reviewMessageForApplicant).build();
            final Event eventStub = mock(Event.class);
            final UUID userId = UUID.randomUUID();
            final OldCompanyBillingProfile initialCompanyBillingProfile =
                    OldCompanyBillingProfile.builder()
                            .id(billingProfileId)
                            .userId(userId)
                            .reviewMessageForApplicant(reviewMessageForApplicant)
                            .externalApplicantId(applicantId)
                            .status(OldVerificationStatus.STARTED).build();
            final OldCompanyBillingProfile companyBillingProfileWithNewStatus =
                    initialCompanyBillingProfile.toBuilder()
                            .status(OldVerificationStatus.REJECTED)
                            .build();
            final OldCompanyBillingProfile updatedCompanyBillingProfile =
                    initialCompanyBillingProfile.toBuilder()
                            .id(billingProfileId)
                            .userId(userId)
                            .status(OldVerificationStatus.REJECTED)
                            .address(faker.address().fullAddress())
                            .oldCountry(OldCountry.fromIso3(faker.address().countryCode()))
                            .name(faker.rickAndMorty().character())
                            .euVATNumber(faker.hacker().abbreviation())
                            .subjectToEuropeVAT(true)
                            .registrationDate(new Date())
                            .registrationNumber(faker.harryPotter().character())
                            .usEntity(false)
                            .build();
            final User user =
                    User.builder().id(userId).githubUserId(1L).githubLogin(faker.rickAndMorty().character()).githubAvatarUrl(faker.internet().url()).githubEmail(faker.internet().emailAddress()).build();

            // When
            when(billingProfileExternalMapper.apply(eventStub)).thenReturn(event);
            when(oldBillingProfileStoragePort.findCompanyProfileById(billingProfileId)).thenReturn(Optional.of(initialCompanyBillingProfile));
            when(userVerificationStoragePort.updateCompanyVerification(companyBillingProfileWithNewStatus)).thenReturn(updatedCompanyBillingProfile);
            when(userStoragePort.getUserById(userId)).thenReturn(Optional.of(user));
            when(oldBillingProfileStoragePort.saveCompanyProfile(updatedCompanyBillingProfile)).thenReturn(updatedCompanyBillingProfile);
            when(oldBillingProfileStoragePort.findKycStatusesFromParentKybExternalVerificationId(initialCompanyBillingProfile.getExternalApplicantId()))
                    .thenReturn(List.of(OldVerificationStatus.NOT_STARTED));
            userVerificationService.process(eventStub);

            // Then
            verify(userVerificationStoragePort, times(1)).updateCompanyVerification(companyBillingProfileWithNewStatus);
            verify(oldBillingProfileStoragePort, times(1)).saveCompanyProfile(updatedCompanyBillingProfile);
            final ArgumentCaptor<BillingProfileUpdated> billingProfileUpdatedArgumentCaptor = ArgumentCaptor.forClass(BillingProfileUpdated.class);
            verify(notificationPort, times(1)).notifyNewVerificationEvent(billingProfileUpdatedArgumentCaptor.capture());
            assertEquals(user.getId(), billingProfileUpdatedArgumentCaptor.getValue().getUserId());
            assertEquals(user.getGithubAvatarUrl(), billingProfileUpdatedArgumentCaptor.getValue().getGithubAvatarUrl());
            assertEquals(user.getGithubLogin(), billingProfileUpdatedArgumentCaptor.getValue().getGithubLogin());
            assertEquals(user.getGithubEmail(), billingProfileUpdatedArgumentCaptor.getValue().getGithubUserEmail());
            assertEquals(user.getGithubUserId(), billingProfileUpdatedArgumentCaptor.getValue().getGithubUserId());
            verify(accountingUserObserverPort).onBillingProfileUpdated(billingProfileUpdatedArgumentCaptor.getValue());
            verify(webhookPort, times(1)).send(billingProfileUpdatedArgumentCaptor.getValue());
            verify(oldBillingProfileStoragePort).findKycStatusesFromParentKybExternalVerificationId(initialCompanyBillingProfile.getExternalApplicantId());
        }

        @Test
        void should_update_existing_company_profile_with_verification_data_given_user_not_found() {
            // Given
            final UUID billingProfileId = UUID.randomUUID();
            final String reviewMessageForApplicant = faker.rickAndMorty().location();
            final BillingProfileUpdated event =
                    BillingProfileUpdated.builder().oldVerificationStatus(OldVerificationStatus.REJECTED).type(OldBillingProfileType.COMPANY).billingProfileId(billingProfileId).reviewMessageForApplicant(reviewMessageForApplicant).build();
            final Event eventStub = mock(Event.class);
            final UUID userId = UUID.randomUUID();
            final OldCompanyBillingProfile initialCompanyBillingProfile =
                    OldCompanyBillingProfile.builder().id(billingProfileId).userId(userId).reviewMessageForApplicant(reviewMessageForApplicant).status(OldVerificationStatus.STARTED).build();
            final OldCompanyBillingProfile companyBillingProfileWithNewStatus =
                    initialCompanyBillingProfile.toBuilder().status(OldVerificationStatus.REJECTED).build();
            final OldCompanyBillingProfile updatedCompanyBillingProfile =
                    initialCompanyBillingProfile.toBuilder().id(billingProfileId).userId(userId).status(OldVerificationStatus.REJECTED).address(faker.address().fullAddress()).oldCountry(OldCountry.fromIso3(faker.address().country()))
                            .name(faker.rickAndMorty().character())
                            .euVATNumber(faker.hacker().abbreviation())
                            .subjectToEuropeVAT(true)
                            .registrationDate(new Date())
                            .registrationNumber(faker.harryPotter().character())
                            .usEntity(false)
                            .build();

            // When
            when(billingProfileExternalMapper.apply(eventStub)).thenReturn(event);
            when(oldBillingProfileStoragePort.findCompanyProfileById(billingProfileId)).thenReturn(Optional.of(initialCompanyBillingProfile));
            when(userVerificationStoragePort.updateCompanyVerification(companyBillingProfileWithNewStatus)).thenReturn(updatedCompanyBillingProfile);
            when(userStoragePort.getUserById(userId)).thenReturn(Optional.empty());
            when(oldBillingProfileStoragePort.saveCompanyProfile(updatedCompanyBillingProfile)).thenReturn(updatedCompanyBillingProfile);
            Exception exception = null;
            try {
                userVerificationService.process(eventStub);
            } catch (Exception e) {
                exception = e;
            }

            // Then
            verify(userVerificationStoragePort, times(1)).updateCompanyVerification(companyBillingProfileWithNewStatus);
            verify(oldBillingProfileStoragePort, times(1)).saveCompanyProfile(updatedCompanyBillingProfile);
            verifyNoInteractions(notificationPort);
            verifyNoInteractions(webhookPort);
            verifyNoInteractions(accountingUserObserverPort);
            assertNotNull(exception);
            assertEquals("User %s not found".formatted(userId), exception.getMessage());
        }


        @Test
        void should_raise_exception_to_skip_unknown_external_id_for_company() {
            // Given
            final BillingProfileUpdated event =
                    BillingProfileUpdated.builder().oldVerificationStatus(OldVerificationStatus.REJECTED).type(OldBillingProfileType.COMPANY).billingProfileId(UUID.randomUUID()).reviewMessageForApplicant(faker.rickAndMorty().location()).build();
            final Event eventStub = mock(Event.class);

            // When
            when(billingProfileExternalMapper.apply(eventStub)).thenReturn(event);
            when(oldBillingProfileStoragePort.findCompanyProfileById(UUID.randomUUID())).thenReturn(Optional.empty());
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
            final String reviewMessageForApplicant = faker.rickAndMorty().location();
            final BillingProfileUpdated event =
                    BillingProfileUpdated.builder().oldVerificationStatus(OldVerificationStatus.NOT_STARTED).type(OldBillingProfileType.INDIVIDUAL).billingProfileId(billingProfileId).reviewMessageForApplicant(reviewMessageForApplicant).build();
            final UUID userId = UUID.randomUUID();
            final BillingProfileUpdated eventWithUserId = event.toBuilder().userId(userId).build();
            final Event eventStub = mock(Event.class);
            final OldIndividualBillingProfile initialIndividualBillingProfile =
                    OldIndividualBillingProfile.builder().id(billingProfileId).userId(userId).reviewMessageForApplicant(reviewMessageForApplicant).status(OldVerificationStatus.REJECTED).build();
            final OldIndividualBillingProfile individualBillingProfileWithStatus =
                    initialIndividualBillingProfile.toBuilder().status(OldVerificationStatus.NOT_STARTED).build();
            final OldIndividualBillingProfile updatedIndividualBillingProfile =
                    individualBillingProfileWithStatus.toBuilder().oldCountry(OldCountry.fromIso3(faker.address().country()))
                            .address(faker.address().fullAddress())
                            .birthdate(new Date())
                            .idDocumentNumber(faker.idNumber().valid())
                            .idDocumentType(OldIndividualBillingProfile.OldIdDocumentTypeEnum.PASSPORT)
                            .usCitizen(false)
                            .validUntil(new Date())
                            .build();
            final User user = User.builder()
                    .id(userId)
                    .githubUserId(1L)
                    .githubLogin(faker.rickAndMorty().character())
                    .githubAvatarUrl(faker.internet().url())
                    .githubEmail(faker.internet().emailAddress())
                    .build();

            // When
            when(billingProfileExternalMapper.apply(eventStub)).thenReturn(event);
            when(oldBillingProfileStoragePort.findIndividualProfileById(billingProfileId)).thenReturn(Optional.of(initialIndividualBillingProfile));
            when(userVerificationStoragePort.updateIndividualVerification(individualBillingProfileWithStatus)).thenReturn(updatedIndividualBillingProfile);
            when(oldBillingProfileStoragePort.saveIndividualProfile(updatedIndividualBillingProfile)).thenReturn(updatedIndividualBillingProfile);
            when(userStoragePort.getUserById(userId)).thenReturn(Optional.of(user));
            userVerificationService.process(eventStub);

            // Then
            verify(userVerificationStoragePort, times(1)).updateIndividualVerification(individualBillingProfileWithStatus);
            verify(oldBillingProfileStoragePort, times(1)).saveIndividualProfile(updatedIndividualBillingProfile);
            final ArgumentCaptor<BillingProfileUpdated> billingProfileUpdatedArgumentCaptor = ArgumentCaptor.forClass(BillingProfileUpdated.class);
            verify(notificationPort, times(1)).notifyNewVerificationEvent(billingProfileUpdatedArgumentCaptor.capture());
            assertEquals(user.getId(), billingProfileUpdatedArgumentCaptor.getValue().getUserId());
            assertEquals(user.getGithubAvatarUrl(), billingProfileUpdatedArgumentCaptor.getValue().getGithubAvatarUrl());
            assertEquals(user.getGithubLogin(), billingProfileUpdatedArgumentCaptor.getValue().getGithubLogin());
            assertEquals(user.getGithubEmail(), billingProfileUpdatedArgumentCaptor.getValue().getGithubUserEmail());
            assertEquals(user.getGithubUserId(), billingProfileUpdatedArgumentCaptor.getValue().getGithubUserId());
            verify(webhookPort, times(1)).send(billingProfileUpdatedArgumentCaptor.getValue());
            verify(accountingUserObserverPort).onBillingProfileUpdated(billingProfileUpdatedArgumentCaptor.getValue());
        }

        @Test
        void should_update_existing_individual_profile_with_verification_data_given_user_not_found() {
            // Given
            final UUID billingProfileId = UUID.randomUUID();
            final String reviewMessageForApplicant = faker.rickAndMorty().location();
            final BillingProfileUpdated event =
                    BillingProfileUpdated.builder().oldVerificationStatus(OldVerificationStatus.NOT_STARTED).type(OldBillingProfileType.INDIVIDUAL).billingProfileId(billingProfileId).reviewMessageForApplicant(reviewMessageForApplicant).build();
            final Event eventStub = mock(Event.class);
            final UUID userId = UUID.randomUUID();
            final BillingProfileUpdated eventWithUserId = event.toBuilder().userId(userId).build();
            final OldIndividualBillingProfile initialIndividualBillingProfile =
                    OldIndividualBillingProfile.builder().id(billingProfileId).userId(userId).reviewMessageForApplicant(reviewMessageForApplicant).status(OldVerificationStatus.REJECTED).build();
            final OldIndividualBillingProfile individualBillingProfileWithStatus =
                    initialIndividualBillingProfile.toBuilder().status(OldVerificationStatus.NOT_STARTED).build();
            final OldIndividualBillingProfile updatedIndividualBillingProfile =
                    individualBillingProfileWithStatus.toBuilder().oldCountry(OldCountry.fromIso3(faker.address().country()))
                            .address(faker.address().fullAddress())
                            .birthdate(new Date())
                            .idDocumentNumber(faker.idNumber().valid())
                            .idDocumentType(OldIndividualBillingProfile.OldIdDocumentTypeEnum.PASSPORT)
                            .usCitizen(false)
                            .validUntil(new Date())
                            .build();

            // When
            when(billingProfileExternalMapper.apply(eventStub)).thenReturn(event);
            when(oldBillingProfileStoragePort.findIndividualProfileById(billingProfileId)).thenReturn(Optional.of(initialIndividualBillingProfile));
            when(userVerificationStoragePort.updateIndividualVerification(individualBillingProfileWithStatus)).thenReturn(updatedIndividualBillingProfile);
            when(oldBillingProfileStoragePort.saveIndividualProfile(updatedIndividualBillingProfile)).thenReturn(updatedIndividualBillingProfile);
            when(userStoragePort.getUserById(userId)).thenReturn(Optional.empty());
            Exception exception = null;
            try {
                userVerificationService.process(eventStub);
            } catch (Exception e) {
                exception = e;
            }

            // Then
            verify(userVerificationStoragePort, times(1)).updateIndividualVerification(individualBillingProfileWithStatus);
            verify(oldBillingProfileStoragePort, times(1)).saveIndividualProfile(updatedIndividualBillingProfile);
            verifyNoInteractions(notificationPort);
            verifyNoInteractions(notificationPort);
            verifyNoInteractions(webhookPort);
            verifyNoInteractions(accountingUserObserverPort);
            assertNotNull(exception);
            assertEquals("User %s not found".formatted(userId), exception.getMessage());
        }


        @Test
        void should_raise_exception_to_skip_unknown_external_id_for_individual() {
            // Given
            final BillingProfileUpdated event =
                    BillingProfileUpdated.builder().oldVerificationStatus(OldVerificationStatus.REJECTED).type(OldBillingProfileType.INDIVIDUAL).billingProfileId(UUID.randomUUID()).reviewMessageForApplicant(faker.rickAndMorty().location()).build();
            final Event eventStub = mock(Event.class);

            // When
            when(billingProfileExternalMapper.apply(eventStub)).thenReturn(event);
            when(oldBillingProfileStoragePort.findIndividualProfileById(UUID.randomUUID())).thenReturn(Optional.empty());
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

    @Nested
    class GivenAChildrenOldBillingProfile {
        private UserVerificationService userVerificationService;
        private OldBillingProfileStoragePort oldBillingProfileStoragePort;
        private OutboxPort outboxPort;
        private Function<Event, BillingProfileUpdated> billingProfileExternalMapper;
        private AccountingUserObserverPort accountingUserObserverPort;
        private UserVerificationStoragePort userVerificationStoragePort;
        private NotificationPort notificationPort;
        private UserStoragePort userStoragePort;
        private WebhookPort webhookPort;
        private final Faker faker = new Faker();

        @BeforeEach
        void setUp() {
            oldBillingProfileStoragePort = mock(OldBillingProfileStoragePort.class);
            outboxPort = mock(OutboxPort.class);
            billingProfileExternalMapper = mock(Function.class);
            accountingUserObserverPort = mock(AccountingUserObserverPort.class);
            userVerificationStoragePort = mock(UserVerificationStoragePort.class);
            notificationPort = mock(NotificationPort.class);
            userStoragePort = mock(UserStoragePort.class);
            webhookPort = mock(WebhookPort.class);
            userVerificationService = new UserVerificationService(outboxPort, billingProfileExternalMapper, oldBillingProfileStoragePort,
                    userVerificationStoragePort, accountingUserObserverPort, notificationPort, userStoragePort, webhookPort);
        }

        @Test
        void should_skip_update_parent_billing_profile_given_parent_billing_profile_not_found() {
            // Given
            final BillingProfileUpdated mappedEvent =
                    BillingProfileUpdated.builder().type(OldBillingProfileType.INDIVIDUAL).oldVerificationStatus(OldVerificationStatus.STARTED).parentExternalApplicantId(faker.rickAndMorty().character()).externalApplicantId(faker.gameOfThrones().character()).build();
            final Event event = mock(Event.class);

            // When
            when(billingProfileExternalMapper.apply(event)).thenReturn(mappedEvent);
            when(oldBillingProfileStoragePort.findCompanyByExternalVerificationId(mappedEvent.getParentExternalApplicantId())).thenReturn(Optional.empty());
            Exception e = null;
            try {
                userVerificationService.process(event);
            } catch (Exception exception) {
                e = exception;
            }

            // Then
            assertNotNull(e);
            assertTrue(e instanceof OutboxSkippingException);
            assertEquals("Parent billing profile not found for external parent id %s".formatted(mappedEvent.getParentExternalApplicantId()), e.getMessage());
        }

        @Test
        void should_skip_update_parent_billing_profile_given_invalid_children_event_for_company_type() {
            // Given
            final String parentBillingProfileExternalVerificationId = faker.rickAndMorty().character();
            final BillingProfileUpdated mappedEvent =
                    BillingProfileUpdated.builder().type(OldBillingProfileType.COMPANY).oldVerificationStatus(OldVerificationStatus.STARTED).parentExternalApplicantId(parentBillingProfileExternalVerificationId).externalApplicantId(faker.gameOfThrones().character()).build();
            final Event event = mock(Event.class);

            // When
            when(billingProfileExternalMapper.apply(event)).thenReturn(mappedEvent);
            Exception e = null;
            try {
                userVerificationService.process(event);
            } catch (Exception exception) {
                e = exception;
            }

            // Then
            assertNotNull(e);
            assertTrue(e instanceof OutboxSkippingException);
            assertEquals("Invalid children billing profile for type %s and external parent id %s".formatted(OldBillingProfileType.COMPANY,
                    parentBillingProfileExternalVerificationId), e.getMessage());
        }

        @Test
        void should_update_parent_billing_profile_status_and_children_profile_statuses() {
            // Given
            final String parentBillingProfileExternalVerificationId = faker.rickAndMorty().character();
            final BillingProfileUpdated mappedEvent =
                    BillingProfileUpdated.builder().type(OldBillingProfileType.INDIVIDUAL)
                            .oldVerificationStatus(OldVerificationStatus.STARTED)
                            .parentExternalApplicantId(parentBillingProfileExternalVerificationId)
                            .externalApplicantId(faker.gameOfThrones().character())
                            .rawReviewDetails(faker.rickAndMorty().location()).build();
            final Event event = mock(Event.class);
            final UUID userId = UUID.randomUUID();
            final OldCompanyBillingProfile companyBillingProfile =
                    OldCompanyBillingProfile.builder().status(OldVerificationStatus.STARTED).userId(userId).id(UUID.randomUUID()).build();
            final OldCompanyBillingProfile updatedBillingProfile = companyBillingProfile.toBuilder().status(OldVerificationStatus.REJECTED).build();
            final User user =
                    User.builder().id(userId).githubUserId(1L).githubLogin(faker.rickAndMorty().character()).githubAvatarUrl(faker.internet().url()).githubEmail(faker.internet().emailAddress()).build();

            // When
            when(billingProfileExternalMapper.apply(event)).thenReturn(mappedEvent);
            when(oldBillingProfileStoragePort.findCompanyByExternalVerificationId(mappedEvent.getParentExternalApplicantId())).thenReturn(Optional.of(companyBillingProfile));
            when(userVerificationStoragePort.updateCompanyVerification(companyBillingProfile))
                    .thenReturn(updatedBillingProfile);
            when(oldBillingProfileStoragePort.findKycStatusesFromParentKybExternalVerificationId(mappedEvent.getParentExternalApplicantId())).thenReturn(List.of(OldVerificationStatus.REJECTED, OldVerificationStatus.VERIFIED));
            when(oldBillingProfileStoragePort.saveCompanyProfile(updatedBillingProfile)).thenReturn(updatedBillingProfile);
            when(userStoragePort.getUserById(userId)).thenReturn(Optional.of(user));
            userVerificationService.process(event);

            // Then
            verify(oldBillingProfileStoragePort).saveChildrenKyc(mappedEvent.getExternalApplicantId(), mappedEvent.getParentExternalApplicantId(),
                    mappedEvent.getOldVerificationStatus());
            final BillingProfileUpdated billingProfileUpdated =
                    BillingProfileUpdated.builder().billingProfileId(companyBillingProfile.getId())
                            .externalApplicantId(companyBillingProfile.getExternalApplicantId())
                            .type(OldBillingProfileType.COMPANY)
                            .oldVerificationStatus(OldVerificationStatus.REJECTED)
                            .userId(userId)
                            .githubUserId(user.getGithubUserId())
                            .githubLogin(user.getGithubLogin())
                            .githubUserEmail(user.getGithubEmail())
                            .githubAvatarUrl(user.getGithubAvatarUrl())
                            .externalApplicantId(mappedEvent.getExternalApplicantId())
                            .parentExternalApplicantId(mappedEvent.getParentExternalApplicantId())
                            .rawReviewDetails(mappedEvent.getRawReviewDetails()).build();
            verify(notificationPort).notifyNewVerificationEvent(billingProfileUpdated);
            verify(webhookPort).send(billingProfileUpdated);
            verify(accountingUserObserverPort).onBillingProfileUpdated(billingProfileUpdated);
        }
    }
}
