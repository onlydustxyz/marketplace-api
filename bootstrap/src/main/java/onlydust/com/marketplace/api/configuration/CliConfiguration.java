package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.api.postgres.adapter.repository.WalletRepository;
import onlydust.com.marketplace.cli.AccountBookDisplay;
import onlydust.com.marketplace.cli.EvmWalletSanitizer;
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
    public EvmWalletSanitizer evmWalletSanitizer(final WalletRepository walletRepository) {
        return new EvmWalletSanitizer(walletRepository);
    }

}
