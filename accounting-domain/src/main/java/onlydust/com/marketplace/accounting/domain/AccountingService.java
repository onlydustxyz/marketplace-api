package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.CommitteeAccountingStoragePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.math.BigDecimal;
import java.util.UUID;


@AllArgsConstructor
public class AccountingService implements AccountingFacadePort {
    private final CommitteeAccountingStoragePort committeeAccountingStoragePort;

    @Override
    public void registerTransferFromSponsor(UUID sponsorId, UUID committeeId, BigDecimal amount, Currency currency,
                                            Network network) {

        final var currentBalance = committeeAccountingStoragePort.getBalance(committeeId, currency);
        committeeAccountingStoragePort.saveBalance(committeeId,
                currentBalance.orElse(BigDecimal.ZERO).add(amount),
                currency);
    }

    @Override
    public void allocateFundsToProject(UUID committeeId, UUID projectId, BigDecimal amount, Currency currency) {
        final var balance = committeeAccountingStoragePort.getBalance(committeeId, currency);
        if (balance.isEmpty() || balance.get().compareTo(amount) < 0) {
            throw OnlyDustException.badRequest("Not enough funds");
        }
    }

    @Override
    public void registerRefundToSponsor(UUID sponsorId, BigDecimal one, Currency currency) {

        throw OnlyDustException.badRequest("Not enough funds");
    }
}
