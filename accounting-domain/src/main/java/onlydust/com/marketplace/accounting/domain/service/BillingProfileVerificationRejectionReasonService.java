package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileVerificationRejectionReasonFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;

import java.util.Optional;

@AllArgsConstructor
public class BillingProfileVerificationRejectionReasonService implements BillingProfileVerificationRejectionReasonFacadePort {

    private final BillingProfileStoragePort billingProfileStoragePort;

    @Override
    public Optional<String> findExternalRejectionReason(String groupId, String buttonId, String label) {
        return billingProfileStoragePort.findExternalRejectionReason(groupId, buttonId, label);
    }
}
