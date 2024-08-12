package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;

@AllArgsConstructor
public class AccountingPermissionService {
    private final SponsorStoragePort sponsorStoragePort;

    public boolean isUserProgramLead(UserId userId, SponsorId programId) {
        return sponsorStoragePort.isAdmin(userId, programId);
    }
}
