package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.api.postgres.adapter.repository.BillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.EventRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import onlydust.com.marketplace.cli.AccountBookDisplay;
import onlydust.com.marketplace.cli.NewAccountingMigration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("cli")
public class CliConfiguration {
    @Bean
    public NewAccountingMigration newAccountingMigration(
            final EventRepository eventRepository,
            final CurrencyStorage currencyStorage,
            final AccountingFacadePort accountingFacadePort,
            final SponsorRepository sponsorRepository,
            final UserViewRepository userViewRepository,
            final BillingProfileRepository billingProfileRepository
    ) {
        return new NewAccountingMigration(eventRepository, currencyStorage, accountingFacadePort, sponsorRepository, userViewRepository,
                billingProfileRepository);
    }

    @Bean
    public AccountBookDisplay accountBookDisplay(final CurrencyStorage currencyStorage, final AccountBookEventStorage accountBookEventStorage) {
        return new AccountBookDisplay(currencyStorage, accountBookEventStorage);
    }
}
