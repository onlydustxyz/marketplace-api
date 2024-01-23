package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.AccountProvider;

import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
public class AccountProviderProxy implements AccountProvider<Object> {
    private final AccountProvider<SponsorId> sponsorAccountProvider;
    private final AccountProvider<CommitteeId> committeeAccountProvider;
    private final AccountProvider<ProjectId> projectAccountProvider;
    private final AccountProvider<ContributorId> contributorAccountProvider;


    @Override
    public Optional<Account.Id> get(Object ownerId, Currency currency) {
        if (ownerId instanceof SponsorId sponsorId) {
            return sponsorAccountProvider.get(sponsorId, currency);
        } else if (ownerId instanceof CommitteeId committeeId) {
            return committeeAccountProvider.get(committeeId, currency);
        } else if (ownerId instanceof ProjectId projectId) {
            return projectAccountProvider.get(projectId, currency);
        } else if (ownerId instanceof ContributorId contributorId) {
            return contributorAccountProvider.get(contributorId, currency);
        } else {
            throw badRequest("Unknown owner type: " + ownerId.getClass());
        }
    }

    @Override
    public Account.Id create(Object ownerId, Currency currency) {
        if (ownerId instanceof SponsorId sponsorId) {
            return sponsorAccountProvider.create(sponsorId, currency);
        } else if (ownerId instanceof CommitteeId committeeId) {
            return committeeAccountProvider.create(committeeId, currency);
        } else if (ownerId instanceof ProjectId projectId) {
            return projectAccountProvider.create(projectId, currency);
        } else if (ownerId instanceof ContributorId contributorId) {
            return contributorAccountProvider.create(contributorId, currency);
        } else {
            throw badRequest("Unknown owner type: " + ownerId.getClass());
        }
    }
}
