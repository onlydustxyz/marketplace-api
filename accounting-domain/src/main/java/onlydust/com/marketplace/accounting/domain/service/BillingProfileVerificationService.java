package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.notification.dto.NotificationBillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileVerificationFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileVerificationProviderPort;
import onlydust.com.marketplace.kernel.jobs.OutboxSkippingException;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class BillingProfileVerificationService implements BillingProfileVerificationFacadePort, OutboxConsumer {

    private final OutboxPort outboxPort;
    private final Function<Event, BillingProfileVerificationUpdated> billingProfileVerificationExternalMapper;
    private final BillingProfileStoragePort billingProfileStoragePort;
    private final BillingProfileVerificationProviderPort billingProfileVerificationProviderPort;
    private final BillingProfileObserverPort billingProfileObserver;

    @Override
    public void consumeBillingProfileVerificationEvent(Event event) {
        outboxPort.push(event);
    }

    @Override
    @Transactional
    public void process(Event event) {
        BillingProfileVerificationUpdated billingProfileVerificationUpdated = billingProfileVerificationExternalMapper.apply(event);
        if (billingProfileVerificationUpdated.isAChildrenKYC()) {
            billingProfileVerificationUpdated = processChildrenKYC(billingProfileVerificationUpdated);
        } else {
            billingProfileVerificationUpdated = processParentBillingProfileVerification(billingProfileVerificationUpdated);
        }
        billingProfileObserver.onBillingProfileUpdated(billingProfileVerificationUpdated);
    }

    private BillingProfileVerificationUpdated processParentBillingProfileVerification(final BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
        return switch (billingProfileVerificationUpdated.getType()) {
            case KYC -> updateBillingProfileWithKyc(billingProfileVerificationUpdated);
            case KYB -> updateBillingProfileWithKyb(billingProfileVerificationUpdated);
        };
    }

    private BillingProfileVerificationUpdated updateBillingProfileWithKyb(BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
        final Kyb updatedKyb = billingProfileStoragePort.findKybById(billingProfileVerificationUpdated.getVerificationId())
                .map(billingProfileVerificationProviderPort::getUpdatedKyb)
                .map(kyb -> kyb.toBuilder()
                        .status(billingProfileVerificationUpdated.getVerificationStatus())
                        .externalApplicantId(billingProfileVerificationUpdated.getExternalApplicantId())
                        .reviewMessageForApplicant(billingProfileVerificationUpdated.getReviewMessageForApplicant())
                        .build())
                .orElseThrow(() -> new OutboxSkippingException("Kyb %s not found".formatted(billingProfileVerificationUpdated.getVerificationId())));
        billingProfileStoragePort.saveKyb(updatedKyb);
        final List<VerificationStatus> childrenKycStatus = billingProfileStoragePort.findAllChildrenKycStatuesFromParentKyb(updatedKyb);
        final VerificationStatus newVerificationStatus = computeBillingProfileStatusFromKybAndChildrenKycs(updatedKyb.getStatus(), childrenKycStatus);
        billingProfileStoragePort.updateBillingProfileStatus(updatedKyb.getBillingProfileId(),
                newVerificationStatus);
        return billingProfileVerificationUpdated.toBuilder()
                .billingProfileId(updatedKyb.getBillingProfileId())
                .verificationStatus(newVerificationStatus)
                .userId(updatedKyb.getOwnerId())
                .build();
    }

    protected VerificationStatus computeBillingProfileStatusFromKybAndChildrenKycs(VerificationStatus kybStatus, List<VerificationStatus> childrenKycStatus) {
        if (isNull(childrenKycStatus) || childrenKycStatus.isEmpty()) {
            return kybStatus;
        }
        final List<VerificationStatus> sortedChildrenKycs =
                childrenKycStatus.stream().sorted(Comparator.comparingInt(VerificationStatus::getPriority)).collect(Collectors.toList());
        Collections.reverse(sortedChildrenKycs);
        final VerificationStatus worstChildrenVerificationStatus = sortedChildrenKycs.get(0);
        if (kybStatus.getPriority() >= worstChildrenVerificationStatus.getPriority()) {
            return kybStatus;
        } else {
            return worstChildrenVerificationStatus;
        }
    }

    private BillingProfileVerificationUpdated updateBillingProfileWithKyc(BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
        final Kyc updatedKyc = billingProfileStoragePort.findKycById(billingProfileVerificationUpdated.getVerificationId())
                .map(billingProfileVerificationProviderPort::getUpdatedKyc)
                .map(kyc -> kyc.toBuilder()
                        .status(billingProfileVerificationUpdated.getVerificationStatus())
                        .externalApplicantId(billingProfileVerificationUpdated.getExternalApplicantId())
                        .reviewMessageForApplicant(billingProfileVerificationUpdated.getReviewMessageForApplicant())
                        .build()
                )
                .orElseThrow(() -> new OutboxSkippingException("Kyc %s not found".formatted(billingProfileVerificationUpdated.getVerificationId())));
        billingProfileStoragePort.saveKyc(updatedKyc);
        billingProfileStoragePort.updateBillingProfileStatus(updatedKyc.getBillingProfileId(), updatedKyc.getStatus());
        return billingProfileVerificationUpdated.toBuilder()
                .billingProfileId(updatedKyc.getBillingProfileId())
                .userId(updatedKyc.getOwnerId())
                .verificationStatus(updatedKyc.getStatus())
                .build();
    }


    private BillingProfileVerificationUpdated processChildrenKYC(final BillingProfileVerificationUpdated childrenBillingProfileUpdated) {
        final Kyb parentKyb = billingProfileStoragePort.findKybByParentExternalId(childrenBillingProfileUpdated.getParentExternalApplicantId())
                .orElseThrow(() -> new OutboxSkippingException("Parent Kyb not found for external parent external id %s"
                        .formatted(childrenBillingProfileUpdated.getParentExternalApplicantId())));
        if (childrenBillingProfileUpdated.getVerificationStatus().equals(VerificationStatus.STARTED)) {
            processStartedChildrenKyc(childrenBillingProfileUpdated, parentKyb);
        }
        billingProfileStoragePort.saveChildrenKyc(childrenBillingProfileUpdated.getExternalApplicantId(),
                childrenBillingProfileUpdated.getParentExternalApplicantId(), childrenBillingProfileUpdated.getVerificationStatus());
        final List<VerificationStatus> childrenKycStatus = billingProfileStoragePort.findAllChildrenKycStatuesFromParentKyb(parentKyb);
        final VerificationStatus newVerificationStatus = computeBillingProfileStatusFromKybAndChildrenKycs(parentKyb.getStatus(), childrenKycStatus);
        billingProfileStoragePort.updateBillingProfileStatus(parentKyb.getBillingProfileId(),
                newVerificationStatus);
        return childrenBillingProfileUpdated.toBuilder()
                .billingProfileId(parentKyb.getBillingProfileId())
                .verificationStatus(newVerificationStatus)
                .userId(parentKyb.getOwnerId())
                .verificationId(parentKyb.getId())
                .build();
    }

    private void processStartedChildrenKyc(final BillingProfileVerificationUpdated childrenBillingProfileUpdated, final Kyb parentKyb) {
        final IndividualKycIdentity individualKycIdentity =
                billingProfileVerificationProviderPort.getIndividualIdentityForKycId(childrenBillingProfileUpdated.getExternalApplicantId())
                        .orElseThrow(() -> new OutboxSkippingException("Kyc identity not found for external applicant id %s"
                                .formatted(childrenBillingProfileUpdated.getExternalApplicantId())));
        final String externalVerificationLink =
                billingProfileVerificationProviderPort.getExternalVerificationLink(childrenBillingProfileUpdated.getExternalUserId())
                        .orElseThrow(() -> new OutboxSkippingException("External verification link not found for external user id %s"
                                .formatted(childrenBillingProfileUpdated.getExternalUserId())));
        billingProfileObserver.onBillingProfileExternalVerificationRequested(
                new BillingProfileChildrenKycVerification(new NotificationBillingProfile(parentKyb.getBillingProfileId().value(), parentKyb.getName()),
                        individualKycIdentity, externalVerificationLink));
    }
}
