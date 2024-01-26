package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;

import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
public class LedgerProviderProxy implements LedgerProvider<Object> {
    private final LedgerProvider<SponsorId> sponsorLedgerProvider;
    private final LedgerProvider<CommitteeId> committeeLedgerProvider;
    private final LedgerProvider<ProjectId> projectLedgerProvider;
    private final LedgerProvider<ContributorId> contributorLedgerProvider;


    @Override
    public Optional<Ledger> get(Object ownerId, Currency currency) {
        if (ownerId instanceof SponsorId sponsorId) {
            return sponsorLedgerProvider.get(sponsorId, currency);
        } else if (ownerId instanceof CommitteeId committeeId) {
            return committeeLedgerProvider.get(committeeId, currency);
        } else if (ownerId instanceof ProjectId projectId) {
            return projectLedgerProvider.get(projectId, currency);
        } else if (ownerId instanceof ContributorId contributorId) {
            return contributorLedgerProvider.get(contributorId, currency);
        } else {
            throw badRequest("Unknown owner type: " + ownerId.getClass());
        }
    }

    @Override
    public Ledger create(Object ownerId, Currency currency) {
        if (ownerId instanceof SponsorId sponsorId) {
            return sponsorLedgerProvider.create(sponsorId, currency);
        } else if (ownerId instanceof CommitteeId committeeId) {
            return committeeLedgerProvider.create(committeeId, currency);
        } else if (ownerId instanceof ProjectId projectId) {
            return projectLedgerProvider.create(projectId, currency);
        } else if (ownerId instanceof ContributorId contributorId) {
            return contributorLedgerProvider.create(contributorId, currency);
        } else {
            throw badRequest("Unknown owner type: " + ownerId.getClass());
        }
    }
}
