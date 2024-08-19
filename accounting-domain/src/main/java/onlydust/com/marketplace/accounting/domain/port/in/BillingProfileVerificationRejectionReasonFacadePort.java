package onlydust.com.marketplace.accounting.domain.port.in;

import java.util.Optional;

public interface BillingProfileVerificationRejectionReasonFacadePort {

    Optional<String> findExternalRejectionReason(String groupId, String buttonId, String label);
}
