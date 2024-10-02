package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookProjector;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.api.postgres.adapter.repository.AllTransactionRepository;
import onlydust.com.marketplace.cli.AccountBookDisplay;
import onlydust.com.marketplace.cli.AccountBookRefresh;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("cli")
public class CliConfiguration {
    @Bean
    public AccountBookDisplay accountBookDisplay(final CurrencyStorage currencyStorage, final SponsorAccountStorage sponsorAccountStorage,
                                                 final CachedAccountBookProvider accountBookProvider) {
        return new AccountBookDisplay(currencyStorage, sponsorAccountStorage, accountBookProvider);
    }

    @Bean
    public AccountBookRefresh accountBookRefresh(final CurrencyStorage currencyStorage,
                                                 final AccountBookStorage accountBookStorage,
                                                 final AccountBookEventStorage accountBookEventStorage,
                                                 final AccountBookProjector accountBookProjector,
                                                 final AllTransactionRepository allTransactionRepository) {
        return new AccountBookRefresh(currencyStorage, accountBookStorage, accountBookEventStorage, accountBookProjector, allTransactionRepository);
    }
}
