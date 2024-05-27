package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorAccountRepository;
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
                                                 final AccountBookEventStorage accountBookEventStorage) {
        return new AccountBookDisplay(currencyStorage, sponsorAccountStorage, accountBookEventStorage);
    }

    @Bean
    public EvmWalletSanitizer evmWalletSanitizer(final WalletRepository walletRepository) {
        return new EvmWalletSanitizer(walletRepository);
    }

}
