package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.LedgerProviderProxy;
import onlydust.com.marketplace.accounting.domain.model.ContributorId;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerStorage;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountingConfiguration {
    @Bean
    public AccountingFacadePort accountingFacadePort(final @NonNull AccountBookEventStorage accountBookEventStorage,
                                                     final @NonNull LedgerProvider<Object> ledgerProvider,
                                                     final @NonNull LedgerStorage ledgerStorage,
                                                     final @NonNull CurrencyStorage currencyStorage) {
        return new AccountingService(accountBookEventStorage, ledgerProvider, ledgerStorage, currencyStorage);
    }

    @Bean
    public LedgerProvider<Object> ledgerProvider(
            final @NonNull LedgerProvider<SponsorId> sponsorLedgerProvider,
            final @NonNull LedgerProvider<ProjectId> projectLedgerProvider,
            final @NonNull LedgerProvider<ContributorId> contributorLedgerProvider
    ) {
        return new LedgerProviderProxy(sponsorLedgerProvider, null, projectLedgerProvider, contributorLedgerProvider);
    }
}
