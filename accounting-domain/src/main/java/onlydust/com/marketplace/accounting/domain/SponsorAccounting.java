package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorAccountingFacadePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@AllArgsConstructor
public class SponsorAccounting implements SponsorAccountingFacadePort {

    private final SponsorAccountProvider sponsorAccountProvider;
    private final CommitteeAccountProvider committeeAccountProvider;

    @Override
    public void registerTransfer(SponsorId sponsorId, Amount amount) {
        final var sponsorAccount = getAccount(sponsorId, amount.getCurrency());
        try {
            sponsorAccount.registerExternalTransfer(amount);
        } catch (OnlyDustException e) {
            throw OnlyDustException.badRequest("Cannot register transfer of %s for sponsor %s: %s"
                    .formatted(amount, sponsorId, e.getMessage()));
        }
    }

    @Override
    public void fundCommittee(SponsorId sponsorId, CommitteeId committeeId, PositiveAmount amount) {
        final var sponsorAccount = getAccount(sponsorId, amount.getCurrency());
        final var committeeAccount = getAccount(committeeId, amount.getCurrency());
        try {
            sponsorAccount.sendAmountTo(committeeAccount, amount);
        } catch (OnlyDustException e) {
            throw OnlyDustException.badRequest("Cannot transfer %s from sponsor %s to committee %s: %s"
                    .formatted(amount, sponsorId, committeeId, e.getMessage()));
        }
    }

    @Override
    public void refundFromCommittee(CommitteeId committeeId, SponsorId sponsorId, PositiveAmount amount) {
        final var sponsorAccount = getAccount(sponsorId, amount.getCurrency());
        final var committeeAccount = getAccount(committeeId, amount.getCurrency());
        try {
            committeeAccount.sendRefundTo(sponsorAccount, amount);
        } catch (OnlyDustException e) {
            throw OnlyDustException.badRequest("Cannot transfer %s from committee %s to sponsor %s: %s"
                    .formatted(amount, committeeId, sponsorId, e.getMessage()));
        }
    }

    private Account getAccount(SponsorId sponsorId, Currency currency) {
        return sponsorAccountProvider.get(sponsorId, currency)
                .orElseThrow(() -> OnlyDustException.notFound("Sponsor %s %s account not found".formatted(sponsorId,
                        currency)));
    }

    private Account getAccount(CommitteeId committeeId, Currency currency) {
        return committeeAccountProvider.get(committeeId, currency)
                .orElseThrow(() -> OnlyDustException.notFound("Committee %s %s account not found".formatted(committeeId,
                        currency)));
    }
}
