package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileVerificationProviderPort;
import onlydust.com.marketplace.kernel.jobs.OutboxSkippingException;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class BillingProfileVerificationServiceTest {

    private BillingProfileVerificationService billingProfileVerificationService;
    private BillingProfileStoragePort billingProfileStoragePort;
    private OutboxPort outboxPort;
    private Function<Event, BillingProfileVerificationUpdated> eventBillingProfileVerificationUpdatedFunction;
    private BillingProfileObserverPort billingProfileObserver;
    private BillingProfileVerificationProviderPort billingProfileVerificationProviderPort;
    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        billingProfileStoragePort = mock(BillingProfileStoragePort.class);
        outboxPort = mock(OutboxPort.class);
        eventBillingProfileVerificationUpdatedFunction = mock(Function.class);
        billingProfileObserver = mock(BillingProfileObserverPort.class);
        billingProfileVerificationProviderPort = mock(BillingProfileVerificationProviderPort.class);
        billingProfileVerificationService = new BillingProfileVerificationService(outboxPort, eventBillingProfileVerificationUpdatedFunction,
                billingProfileStoragePort,
                billingProfileVerificationProviderPort, billingProfileObserver);
    }


    @Nested
    class GivenAKyc {


        @Test
        void should_skip_event_given_kyc_not_found() {
            // Given
            final UUID verificationId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = BillingProfileVerificationUpdated.builder()
                    .verificationStatus(VerificationStatus.UNDER_REVIEW)
                    .verificationId(verificationId)
                    .type(VerificationType.KYC)
                    .externalApplicantId(faker.rickAndMorty().location())
                    .externalUserId(faker.lorem().word())
                    .build();
            final Event event = mock(Event.class);

            // When
            when(eventBillingProfileVerificationUpdatedFunction.apply(event))
                    .thenReturn(billingProfileVerificationUpdated);
            when(billingProfileStoragePort.findKycById(verificationId))
                    .thenReturn(Optional.empty());
            Exception exception = null;
            try {
                billingProfileVerificationService.process(event);
            } catch (Exception e) {
                exception = e;
            }

            // Then
            assertTrue(exception instanceof OutboxSkippingException);
            assertEquals("Kyc %s not found".formatted(verificationId), exception.getMessage());
        }

        @Test
        void should_update_kyc_and_billing_profile() {
            final UUID verificationId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = BillingProfileVerificationUpdated.builder()
                    .verificationStatus(VerificationStatus.UNDER_REVIEW)
                    .verificationId(verificationId)
                    .type(VerificationType.KYC)
                    .externalApplicantId(faker.rickAndMorty().location())
                    .reviewMessageForApplicant(faker.gameOfThrones().character())
                    .externalUserId(faker.lorem().word())
                    .build();
            final Event event = mock(Event.class);
            final var billingProfileId = BillingProfile.Id.random();
            final Kyc initialKyc = Kyc.builder()
                    .ownerId(UserId.random())
                    .billingProfileId(billingProfileId)
                    .id(verificationId)
                    .status(VerificationStatus.NOT_STARTED)
                    .build();
            final Kyc kycWithDataFromExternalSource = initialKyc.toBuilder()
                    .lastName(faker.name().lastName())
                    .firstName(faker.name().firstName())
                    .externalApplicantId(billingProfileVerificationUpdated.getExternalApplicantId())
                    .reviewMessageForApplicant(billingProfileVerificationUpdated.getReviewMessageForApplicant())
                    .build();
            final BillingProfileVerificationUpdated updatedEvent = billingProfileVerificationUpdated.toBuilder()
                    .userId(initialKyc.getOwnerId())
                    .billingProfileId(billingProfileId)
                    .build();

            // When
            when(eventBillingProfileVerificationUpdatedFunction.apply(event))
                    .thenReturn(billingProfileVerificationUpdated);
            when(billingProfileVerificationProviderPort.getUpdatedKyc(initialKyc))
                    .thenReturn(kycWithDataFromExternalSource);
            when(billingProfileStoragePort.findKycById(verificationId))
                    .thenReturn(Optional.of(initialKyc));
            billingProfileVerificationService.process(event);

            // Then
            verify(billingProfileStoragePort).saveKyc(kycWithDataFromExternalSource.toBuilder()
                    .status(billingProfileVerificationUpdated.getVerificationStatus()).build());
            verify(billingProfileStoragePort).updateBillingProfileStatus(kycWithDataFromExternalSource.getBillingProfileId(),
                    billingProfileVerificationUpdated.getVerificationStatus());
            verify(billingProfileObserver).onBillingProfileUpdated(updatedEvent);
        }
    }

    @Nested
    class GivenAKyb {


        @Test
        void should_skip_event_given_kyb_not_found() {
            // Given
            final UUID verificationId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = BillingProfileVerificationUpdated.builder()
                    .verificationStatus(VerificationStatus.UNDER_REVIEW)
                    .verificationId(verificationId)
                    .type(VerificationType.KYB)
                    .externalApplicantId(faker.rickAndMorty().location())
                    .externalUserId(faker.lorem().word())
                    .build();
            final Event event = mock(Event.class);

            // When
            when(eventBillingProfileVerificationUpdatedFunction.apply(event)).thenReturn(billingProfileVerificationUpdated);
            when(billingProfileStoragePort.findKybById(verificationId))
                    .thenReturn(Optional.empty());
            Exception exception = null;
            try {
                billingProfileVerificationService.process(event);
            } catch (Exception e) {
                exception = e;
            }

            // Then
            assertTrue(exception instanceof OutboxSkippingException);
            assertEquals("Kyb %s not found".formatted(verificationId), exception.getMessage());
        }

        @Test
        void should_update_kyb_and_billing_profile_given_no_children_kycs() {
            // Given
            final UUID verificationId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = BillingProfileVerificationUpdated.builder()
                    .verificationStatus(VerificationStatus.UNDER_REVIEW)
                    .verificationId(verificationId)
                    .externalApplicantId(faker.rickAndMorty().location())
                    .reviewMessageForApplicant(faker.gameOfThrones().character())
                    .type(VerificationType.KYB)
                    .externalUserId(faker.lorem().word())
                    .build();
            final Event event = mock(Event.class);
            final var billingProfileId = BillingProfile.Id.random();
            final Kyb initialKyb = Kyb.builder()
                    .ownerId(UserId.random())
                    .id(verificationId)
                    .billingProfileId(billingProfileId)
                    .status(VerificationStatus.NOT_STARTED)
                    .build();
            final Kyb kybWithDataFromExternalSource = initialKyb.toBuilder()
                    .name(faker.rickAndMorty().character()).build();
            final Kyb updateKyb = kybWithDataFromExternalSource.toBuilder()
                    .status(billingProfileVerificationUpdated.getVerificationStatus())
                    .externalApplicantId(billingProfileVerificationUpdated.getExternalApplicantId())
                    .reviewMessageForApplicant(billingProfileVerificationUpdated.getReviewMessageForApplicant())
                    .build();
            final BillingProfileVerificationUpdated updatedEvent = billingProfileVerificationUpdated.toBuilder()
                    .userId(initialKyb.getOwnerId())
                    .billingProfileId(billingProfileId)
                    .build();

            // When
            when(eventBillingProfileVerificationUpdatedFunction.apply(event)).thenReturn(billingProfileVerificationUpdated);
            when(billingProfileStoragePort.findKybById(verificationId))
                    .thenReturn(Optional.of(initialKyb));
            when(billingProfileVerificationProviderPort.getUpdatedKyb(initialKyb))
                    .thenReturn(kybWithDataFromExternalSource);
            when(billingProfileStoragePort.findAllChildrenKycStatuesFromParentKyb(updateKyb))
                    .thenReturn(List.of());
            billingProfileVerificationService.process(event);

            // Then
            verify(billingProfileStoragePort).saveKyb(updateKyb);
            verify(billingProfileStoragePort).updateBillingProfileStatus(updateKyb.getBillingProfileId(), updateKyb.getStatus());
            verify(billingProfileObserver).onBillingProfileUpdated(updatedEvent);
        }

        @Test
        void should_update_kyb_and_billing_profile_given_children_kycs() {
            // Given
            final UUID verificationId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = BillingProfileVerificationUpdated.builder()
                    .verificationStatus(VerificationStatus.UNDER_REVIEW)
                    .verificationId(verificationId)
                    .type(VerificationType.KYB)
                    .externalApplicantId(faker.rickAndMorty().location())
                    .reviewMessageForApplicant(faker.gameOfThrones().character())
                    .externalUserId(faker.lorem().word())
                    .build();
            final Event event = mock(Event.class);
            final var billingProfileId = BillingProfile.Id.random();
            final Kyb initialKyb = Kyb.builder()
                    .ownerId(UserId.random())
                    .id(verificationId)
                    .billingProfileId(billingProfileId)
                    .status(VerificationStatus.NOT_STARTED)
                    .build();
            final Kyb kybWithDataFromExternalSource = initialKyb.toBuilder()
                    .name(faker.rickAndMorty().character()).build();
            final Kyb updateKyb = kybWithDataFromExternalSource.toBuilder()
                    .status(billingProfileVerificationUpdated.getVerificationStatus())
                    .reviewMessageForApplicant(billingProfileVerificationUpdated.getReviewMessageForApplicant())
                    .externalApplicantId(billingProfileVerificationUpdated.getExternalApplicantId())
                    .build();
            final BillingProfileVerificationUpdated updatedEvent = billingProfileVerificationUpdated.toBuilder()
                    .userId(updateKyb.getOwnerId())
                    .verificationStatus(VerificationStatus.CLOSED)
                    .billingProfileId(billingProfileId)
                    .build();

            // When
            when(eventBillingProfileVerificationUpdatedFunction.apply(event)).thenReturn(billingProfileVerificationUpdated);
            when(billingProfileStoragePort.findKybById(verificationId))
                    .thenReturn(Optional.of(initialKyb));
            when(billingProfileVerificationProviderPort.getUpdatedKyb(initialKyb))
                    .thenReturn(kybWithDataFromExternalSource);
            when(billingProfileStoragePort.findAllChildrenKycStatuesFromParentKyb(updateKyb))
                    .thenReturn(List.of(VerificationStatus.CLOSED, VerificationStatus.REJECTED));
            billingProfileVerificationService.process(event);

            // Then
            verify(billingProfileStoragePort).saveKyb(updateKyb);
            verify(billingProfileStoragePort).updateBillingProfileStatus(updateKyb.getBillingProfileId(), VerificationStatus.CLOSED);
            verify(billingProfileObserver).onBillingProfileUpdated(updatedEvent);
        }
    }

    @Nested
    class GivenAChildrenKyc {

        @Test
        void should_skip_event_given_parent_kyb_not_found() {
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = BillingProfileVerificationUpdated.builder()
                    .verificationStatus(VerificationStatus.UNDER_REVIEW)
                    .externalApplicantId(faker.rickAndMorty().location())
                    .parentExternalApplicantId(faker.rickAndMorty().character())
                    .type(VerificationType.KYC)
                    .externalUserId(faker.lorem().word())
                    .build();
            final Event event = mock(Event.class);

            // When
            when(eventBillingProfileVerificationUpdatedFunction.apply(event))
                    .thenReturn(billingProfileVerificationUpdated);
            when(billingProfileStoragePort.findKybByParentExternalId(billingProfileVerificationUpdated.getParentExternalApplicantId()))
                    .thenReturn(Optional.empty());
            Exception exception = null;
            try {
                billingProfileVerificationService.process(event);
            } catch (Exception e) {
                exception = e;
            }

            // Then
            assertTrue(exception instanceof OutboxSkippingException);
            assertEquals("Parent Kyb not found for external parent external id %s".formatted(billingProfileVerificationUpdated.getParentExternalApplicantId()), exception.getMessage());
        }

        @Test
        void should_update_children_kyc_and_billing_profile() {
            // Given
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = BillingProfileVerificationUpdated.builder()
                    .verificationStatus(VerificationStatus.UNDER_REVIEW)
                    .externalApplicantId(faker.rickAndMorty().location())
                    .parentExternalApplicantId(faker.rickAndMorty().character())
                    .type(VerificationType.KYC)
                    .externalUserId(faker.lorem().word())
                    .build();
            final Event event = mock(Event.class);
            final Kyb initialKyb = Kyb.builder()
                    .billingProfileId(BillingProfile.Id.random())
                    .id(UUID.randomUUID())
                    .ownerId(UserId.random())
                    .status(VerificationStatus.UNDER_REVIEW)
                    .build();
            final BillingProfileVerificationUpdated updatedEvent =
                    billingProfileVerificationUpdated.toBuilder()
                            .userId(initialKyb.getOwnerId())
                            .verificationId(initialKyb.getId())
                            .verificationStatus(VerificationStatus.REJECTED).build();

            // When
            when(eventBillingProfileVerificationUpdatedFunction.apply(event))
                    .thenReturn(billingProfileVerificationUpdated);
            when(billingProfileStoragePort.findKybByParentExternalId(billingProfileVerificationUpdated.getParentExternalApplicantId()))
                    .thenReturn(Optional.of(initialKyb));
            when(billingProfileStoragePort.findAllChildrenKycStatuesFromParentKyb(initialKyb))
                    .thenReturn(List.of(VerificationStatus.REJECTED, billingProfileVerificationUpdated.getVerificationStatus()));

            billingProfileVerificationService.process(event);

            // Then
            verify(billingProfileStoragePort).saveChildrenKyc(billingProfileVerificationUpdated.getExternalApplicantId(),
                    billingProfileVerificationUpdated.getParentExternalApplicantId(), billingProfileVerificationUpdated.getVerificationStatus());
            verify(billingProfileStoragePort).updateBillingProfileStatus(initialKyb.getBillingProfileId(), VerificationStatus.REJECTED);
            billingProfileObserver.onBillingProfileUpdated(updatedEvent);
        }


        @Test
        void should_update_status_given_no_children() {
            assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(), VerificationStatus.STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(), VerificationStatus.UNDER_REVIEW);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(), VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(), VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(), VerificationStatus.VERIFIED);
        }

        @Test
        void should_update_status_from_children_statuses_given_one_children() {
            assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.STARTED), VerificationStatus.STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.UNDER_REVIEW), VerificationStatus.STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.REJECTED), VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.CLOSED), VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.VERIFIED), VerificationStatus.STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.STARTED), VerificationStatus.STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.UNDER_REVIEW), VerificationStatus.UNDER_REVIEW);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.REJECTED), VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.CLOSED), VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.VERIFIED), VerificationStatus.UNDER_REVIEW);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.STARTED), VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.UNDER_REVIEW), VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.REJECTED), VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.CLOSED), VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.VERIFIED), VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.STARTED), VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.UNDER_REVIEW), VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.REJECTED), VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.CLOSED), VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.VERIFIED), VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.STARTED), VerificationStatus.STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.UNDER_REVIEW), VerificationStatus.UNDER_REVIEW);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.REJECTED), VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.CLOSED), VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.VERIFIED), VerificationStatus.VERIFIED);
        }

        @Test
        void should_update_status_from_children_statuses_given_two_children() {
            assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.NOT_STARTED, VerificationStatus.STARTED),
                    VerificationStatus.NOT_STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.UNDER_REVIEW, VerificationStatus.STARTED),
                    VerificationStatus.STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.REJECTED, VerificationStatus.UNDER_REVIEW),
                    VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.CLOSED, VerificationStatus.REJECTED),
                    VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.VERIFIED, VerificationStatus.UNDER_REVIEW),
                    VerificationStatus.STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.STARTED, VerificationStatus.STARTED),
                    VerificationStatus.STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.NOT_STARTED, VerificationStatus.STARTED),
                    VerificationStatus.NOT_STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.REJECTED, VerificationStatus.UNDER_REVIEW),
                    VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.CLOSED, VerificationStatus.UNDER_REVIEW),
                    VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.VERIFIED, VerificationStatus.UNDER_REVIEW),
                    VerificationStatus.UNDER_REVIEW);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.STARTED, VerificationStatus.UNDER_REVIEW),
                    VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.UNDER_REVIEW, VerificationStatus.UNDER_REVIEW),
                    VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.REJECTED, VerificationStatus.UNDER_REVIEW),
                    VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.CLOSED, VerificationStatus.STARTED),
                    VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.VERIFIED, VerificationStatus.UNDER_REVIEW),
                    VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.STARTED, VerificationStatus.UNDER_REVIEW),
                    VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.UNDER_REVIEW, VerificationStatus.REJECTED),
                    VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.REJECTED, VerificationStatus.STARTED),
                    VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.CLOSED, VerificationStatus.VERIFIED),
                    VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.VERIFIED, VerificationStatus.STARTED),
                    VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.STARTED, VerificationStatus.VERIFIED),
                    VerificationStatus.STARTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.UNDER_REVIEW, VerificationStatus.VERIFIED),
                    VerificationStatus.UNDER_REVIEW);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.REJECTED, VerificationStatus.STARTED),
                    VerificationStatus.REJECTED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.CLOSED, VerificationStatus.STARTED),
                    VerificationStatus.CLOSED);
            assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.VERIFIED, VerificationStatus.VERIFIED),
                    VerificationStatus.VERIFIED);
        }


        public void assertUpdatedStatusIsEqualsTo(final VerificationStatus parentStatus, final List<VerificationStatus> childrenStatuses,
                                                  final VerificationStatus expectedVerificationStatus) {
            assertEquals(expectedVerificationStatus, billingProfileVerificationService.computeBillingProfileStatusFromKybAndChildrenKycs(parentStatus,
                    childrenStatuses));
        }

        @Test
        void should_skipped_children_kyc_given_no_ubo_email_found() {
            // Given
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = BillingProfileVerificationUpdated.builder()
                    .verificationStatus(VerificationStatus.STARTED)
                    .externalApplicantId(faker.rickAndMorty().location())
                    .parentExternalApplicantId(faker.rickAndMorty().character())
                    .type(VerificationType.KYC)
                    .externalUserId(faker.lorem().word())
                    .build();
            final Event event = mock(Event.class);
            final Kyb initialKyb = Kyb.builder()
                    .billingProfileId(BillingProfile.Id.random())
                    .id(UUID.randomUUID())
                    .ownerId(UserId.random())
                    .status(VerificationStatus.UNDER_REVIEW)
                    .build();

            // When
            when(eventBillingProfileVerificationUpdatedFunction.apply(event))
                    .thenReturn(billingProfileVerificationUpdated);
            when(billingProfileStoragePort.findKybByParentExternalId(billingProfileVerificationUpdated.getParentExternalApplicantId()))
                    .thenReturn(Optional.of(initialKyb));

            when(billingProfileVerificationProviderPort.getIndividualIdentityForKycId(billingProfileVerificationUpdated.getExternalApplicantId()))
                    .thenReturn(Optional.empty());

            Exception exception = null;
            try {
                billingProfileVerificationService.process(event);
            } catch (Exception e) {
                exception = e;
            }

            // Then
            assertTrue(exception instanceof OutboxSkippingException);
            assertEquals("Kyc identity not found for external applicant id %s".formatted(billingProfileVerificationUpdated.getExternalApplicantId()),
                    exception.getMessage());
        }

        @Test
        void should_skipped_children_kyc_given_no_external_verification_link_found() {
            // Given
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = BillingProfileVerificationUpdated.builder()
                    .verificationStatus(VerificationStatus.STARTED)
                    .externalApplicantId(faker.rickAndMorty().location())
                    .parentExternalApplicantId(faker.rickAndMorty().character())
                    .type(VerificationType.KYC)
                    .externalUserId(faker.lorem().word())
                    .build();
            final Event event = mock(Event.class);
            final Kyb initialKyb = Kyb.builder()
                    .billingProfileId(BillingProfile.Id.random())
                    .id(UUID.randomUUID())
                    .ownerId(UserId.random())
                    .status(VerificationStatus.UNDER_REVIEW)
                    .build();
            final IndividualKycIdentity individualKycIdentity = new IndividualKycIdentity(faker.internet().emailAddress(), faker.name().firstName(),
                    faker.name().lastName());

            // When
            when(eventBillingProfileVerificationUpdatedFunction.apply(event))
                    .thenReturn(billingProfileVerificationUpdated);
            when(billingProfileStoragePort.findKybByParentExternalId(billingProfileVerificationUpdated.getParentExternalApplicantId()))
                    .thenReturn(Optional.of(initialKyb));
            when(billingProfileVerificationProviderPort.getIndividualIdentityForKycId(billingProfileVerificationUpdated.getExternalApplicantId()))
                    .thenReturn(Optional.of(individualKycIdentity));
            when(billingProfileVerificationProviderPort.getExternalVerificationLink(billingProfileVerificationUpdated.getExternalApplicantId()))
                    .thenReturn(Optional.empty());

            Exception exception = null;
            try {
                billingProfileVerificationService.process(event);
            } catch (Exception e) {
                exception = e;
            }

            // Then
            assertTrue(exception instanceof OutboxSkippingException);
            assertEquals("External verification link not found for external user id %s".formatted(billingProfileVerificationUpdated.getExternalUserId()),
                    exception.getMessage());
        }

        @Test
        void should_process_children_kyc() {
            // Given
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = BillingProfileVerificationUpdated.builder()
                    .verificationStatus(VerificationStatus.STARTED)
                    .externalApplicantId(faker.rickAndMorty().location())
                    .parentExternalApplicantId(faker.rickAndMorty().character())
                    .type(VerificationType.KYC)
                    .externalUserId(faker.lorem().word())
                    .build();
            final Event event = mock(Event.class);
            final Kyb initialKyb = Kyb.builder()
                    .billingProfileId(BillingProfile.Id.random())
                    .id(UUID.randomUUID())
                    .ownerId(UserId.random())
                    .name(faker.rickAndMorty().character())
                    .status(VerificationStatus.UNDER_REVIEW)
                    .build();
            final IndividualKycIdentity individualKycIdentity = new IndividualKycIdentity(faker.internet().emailAddress(), faker.name().firstName(),
                    faker.name().lastName());
            final String externalVerificationLink = faker.internet().url();
            final CompanyBillingProfile companyBillingProfile = CompanyBillingProfile.builder()
                    .kyb(initialKyb)
                    .name(faker.rickAndMorty().character())
                    .id(BillingProfile.Id.random())
                    .status(VerificationStatus.UNDER_REVIEW)
                    .enabled(true)
                    .members(Set.of()).build();

            // When
            when(eventBillingProfileVerificationUpdatedFunction.apply(event))
                    .thenReturn(billingProfileVerificationUpdated);
            when(billingProfileStoragePort.findKybByParentExternalId(billingProfileVerificationUpdated.getParentExternalApplicantId()))
                    .thenReturn(Optional.of(initialKyb));
            when(billingProfileVerificationProviderPort.getIndividualIdentityForKycId(billingProfileVerificationUpdated.getExternalApplicantId()))
                    .thenReturn(Optional.of(individualKycIdentity));
            when(billingProfileVerificationProviderPort.getExternalVerificationLink(billingProfileVerificationUpdated.getExternalUserId()))
                    .thenReturn(Optional.of(externalVerificationLink));
            when(billingProfileStoragePort.findById(initialKyb.getBillingProfileId()))
                    .thenReturn(Optional.of(companyBillingProfile));
            billingProfileVerificationService.process(event);

            // Then
            final ArgumentCaptor<BillingProfileChildrenKycVerification> billingProfileChildrenKycVerificationArgumentCaptor =
                    ArgumentCaptor.forClass(BillingProfileChildrenKycVerification.class);
            verify(billingProfileObserver).onBillingProfileExternalVerificationRequested(billingProfileChildrenKycVerificationArgumentCaptor.capture());
            assertEquals(billingProfileChildrenKycVerificationArgumentCaptor.getValue().individualKycIdentity(), individualKycIdentity);
            assertEquals(billingProfileChildrenKycVerificationArgumentCaptor.getValue().billingProfileName(), companyBillingProfile.name());
        }

    }

}
