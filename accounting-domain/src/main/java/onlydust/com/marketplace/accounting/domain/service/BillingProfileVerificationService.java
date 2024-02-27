package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileVerificationFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserver;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileVerificationProviderPort;
import onlydust.com.marketplace.kernel.jobs.OutboxSkippingException;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.kernel.port.output.WebhookPort;

import javax.transaction.Transactional;
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
    private final BillingProfileObserver billingProfileObserver;
    private final NotificationPort notificationPort;
    private final WebhookPort webhookPort;

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
        webhookPort.send(billingProfileVerificationUpdated);
        notificationPort.notifyNewEvent(billingProfileVerificationUpdated);
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
                .map(kyb -> kyb.toBuilder().status(billingProfileVerificationUpdated.getVerificationStatus()).build())
                .orElseThrow(() -> new OutboxSkippingException("Kyb %s not found".formatted(billingProfileVerificationUpdated.getVerificationId())));
        billingProfileStoragePort.saveKyb(updatedKyb);
        final List<VerificationStatus> childrenKycStatus = billingProfileStoragePort.findAllChildrenKycStatuesFromParentKyb(updatedKyb);
        final VerificationStatus newVerificationStatus = computeBillingProfileStatusFromKybAndChildrenKycs(updatedKyb.getStatus(), childrenKycStatus);
        billingProfileStoragePort.updateBillingProfileStatus(updatedKyb.getBillingProfileId(),
                newVerificationStatus);
        return billingProfileVerificationUpdated.toBuilder()
                .verificationStatus(newVerificationStatus)
                .userId(updatedKyb.getOwnerId())
                .build();
    }

    private VerificationStatus computeBillingProfileStatusFromKybAndChildrenKycs(VerificationStatus kybStatus, List<VerificationStatus> childrenKycStatus) {
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
                .map(kyc -> kyc.toBuilder().status(billingProfileVerificationUpdated.getVerificationStatus()).build())
                .orElseThrow(() -> new OutboxSkippingException("Kyc %s not found".formatted(billingProfileVerificationUpdated.getVerificationId())));
        billingProfileStoragePort.saveKyc(updatedKyc);
        billingProfileStoragePort.updateBillingProfileStatus(updatedKyc.getBillingProfileId(), updatedKyc.getStatus());
        return billingProfileVerificationUpdated.toBuilder()
                .userId(updatedKyc.getOwnerId())
                .verificationStatus(updatedKyc.getStatus())
                .build();
    }


    private BillingProfileVerificationUpdated processChildrenKYC(BillingProfileVerificationUpdated childrenBillingProfileUpdated) {
        final Kyb parentKyb = billingProfileStoragePort.findKybByParentExternalId(childrenBillingProfileUpdated.getParentExternalApplicantId())
                .orElseThrow(() -> new OutboxSkippingException("Parent Kyb not found for external parent external id %s"
                        .formatted(childrenBillingProfileUpdated.getParentExternalApplicantId())));
        billingProfileStoragePort.saveChildrenKyc(childrenBillingProfileUpdated.getExternalApplicantId(),
                childrenBillingProfileUpdated.getParentExternalApplicantId(), childrenBillingProfileUpdated.getVerificationStatus());
        final List<VerificationStatus> childrenKycStatus = billingProfileStoragePort.findAllChildrenKycStatuesFromParentKyb(parentKyb);
        final VerificationStatus newVerificationStatus = computeBillingProfileStatusFromKybAndChildrenKycs(parentKyb.getStatus(), childrenKycStatus);
        billingProfileStoragePort.updateBillingProfileStatus(parentKyb.getBillingProfileId(),
                newVerificationStatus);
        return childrenBillingProfileUpdated.toBuilder()
                .verificationStatus(newVerificationStatus)
                .userId(parentKyb.getOwnerId())
                .build();
    }
}
