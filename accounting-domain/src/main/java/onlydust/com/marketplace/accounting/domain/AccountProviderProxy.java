package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.CommitteeId;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountProvider;

import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
public class AccountProviderProxy implements AccountProvider<Object> {
    private final AccountProvider<SponsorId> sponsorAccountProvider;
    private final AccountProvider<CommitteeId> committeeAccountProvider;

    @Override
    public Optional<Account> get(Object ownerId, Currency currency) {
        if (ownerId instanceof SponsorId sponsorId) {
            return sponsorAccountProvider.get(sponsorId, currency);
        } else if (ownerId instanceof CommitteeId committeeId) {
            return committeeAccountProvider.get(committeeId, currency);
        } else {
            throw badRequest("Unknown owner type: " + ownerId.getClass());
        }
    }

    @Override
    public Account create(Object ownerId, Currency currency) {
        if (ownerId instanceof SponsorId sponsorId) {
            return sponsorAccountProvider.create(sponsorId, currency);
        } else if (ownerId instanceof CommitteeId committeeId) {
            return committeeAccountProvider.create(committeeId, currency);
        } else {
            throw badRequest("Unknown owner type: " + ownerId.getClass());
        }
    }
}
