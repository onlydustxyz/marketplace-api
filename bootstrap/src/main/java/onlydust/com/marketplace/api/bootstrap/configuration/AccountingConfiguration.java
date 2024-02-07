package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.api.infrastructure.accounting.AccountingObserverAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountingConfiguration {
    @Bean
    public AccountingFacadePort accountingFacadePort(final @NonNull AccountBookEventStorage accountBookEventStorage,
                                                     final @NonNull SponsorAccountStorage sponsorAccountStorage,
                                                     final @NonNull CurrencyStorage currencyStorage) {
        return new AccountingService(accountBookEventStorage, sponsorAccountStorage, currencyStorage);
    }

    @Bean
    public AccountingObserverAdapter accountingObserverAdapter(final @NonNull RewardStatusStorage rewardStatusStorage,
                                                               final @NonNull CurrencyFacadePort currencyFacadePort) {
        return new AccountingObserverAdapter(rewardStatusStorage, currencyFacadePort);
    }
}
