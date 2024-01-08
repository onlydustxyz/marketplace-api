package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.CommitteeId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorAccountingFacadePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@AllArgsConstructor
public class SponsorAccounting implements SponsorAccountingFacadePort {

    private final SponsorAccountProvider sponsorAccountProvider;
    private final CommitteeAccountProvider committeeAccountProvider;

    @Override
    public void registerTransfer(SponsorId sponsorId, Amount amount) {
        final var sponsorAccount = sponsorAccountProvider.sponsorAccount(sponsorId, amount.getCurrency());
        if (sponsorAccount.isEmpty()) {
            throw OnlyDustException.notFound("Sponsor %s %s account not found".formatted(sponsorId,
                    amount.getCurrency()));
        }
        try {
            sponsorAccount.get().registerTransfer(amount);
        } catch (OnlyDustException e) {
            throw OnlyDustException.badRequest("Cannot register transfer of %s for sponsor %s: %s"
                    .formatted(amount, sponsorId, e.getMessage()));
        }
    }

    @Override
    public void fundCommittee(SponsorId sponsorId, CommitteeId committeeId, Amount amount) {
        final var sponsorAccount = sponsorAccountProvider.sponsorAccount(sponsorId, amount.getCurrency());
        if (sponsorAccount.isEmpty()) {
            throw OnlyDustException.notFound("Sponsor %s %s account not found".formatted(sponsorId,
                    amount.getCurrency()));
        }
        final var committeeAccount = committeeAccountProvider.get(committeeId, amount.getCurrency());
        if (committeeAccount.isEmpty()) {
            throw OnlyDustException.notFound("Committee %s %s account not found".formatted(committeeId,
                    amount.getCurrency()));
        }
        try {
            sponsorAccount.get().transferTo(committeeAccount.get(), amount);
        } catch (OnlyDustException e) {
            throw OnlyDustException.badRequest("Cannot transfer %s from sponsor %s to committee %s: %s"
                    .formatted(amount, sponsorId, committeeId, e.getMessage()));
        }
    }
}
