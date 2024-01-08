package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorAccountingFacadePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@AllArgsConstructor
public class SponsorAccounting implements SponsorAccountingFacadePort {

    private final SponsorAccountProvider sponsorAccountProvider;

    @Override
    public void registerTransfer(SponsorId sponsorId, Amount amount) {
        final var sponsorAccount = sponsorAccountProvider.sponsorAccount(sponsorId, amount.getCurrency());
        if (sponsorAccount.isEmpty()) {
            throw OnlyDustException.notFound("Sponsor %s %s account not found".formatted(sponsorId,
                    amount.getCurrency()));
        }
        sponsorAccount.get().transfer(amount);
    }
}
