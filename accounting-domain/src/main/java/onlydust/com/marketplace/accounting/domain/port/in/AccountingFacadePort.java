package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.Transaction;

import java.time.ZonedDateTime;
import java.util.Collection;

public interface AccountingFacadePort {
    void fund(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId, Network network, ZonedDateTime lockedUntil);

    void withdraw(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId, Network network);

    void pay(ContributorId from, PositiveAmount amount, Currency.Id currencyId, Network network);

    <To> void mint(To to, PositiveAmount amount, Currency.Id currencyId);

    <From> Collection<Transaction> burn(From from, PositiveAmount amount, Currency.Id currencyId);

    <From, To> void transfer(From from, To to, PositiveAmount amount, Currency.Id currencyId);

    <From, To> void refund(From from, To to, PositiveAmount amount, Currency.Id currencyId);
}
