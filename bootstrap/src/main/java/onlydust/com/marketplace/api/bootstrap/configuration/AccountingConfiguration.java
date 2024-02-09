package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.RewardStatusService;
import onlydust.com.marketplace.api.infrastructure.accounting.AccountingObserverAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountingConfiguration {
    @Bean
    public AccountingFacadePort accountingFacadePort(final @NonNull AccountBookEventStorage accountBookEventStorage,
                                                     final @NonNull SponsorAccountStorage sponsorAccountStorage,
                                                     final @NonNull CurrencyStorage currencyStorage,
                                                     final @NonNull RewardStatusService rewardStatusService
    ) {
        return new AccountingService(accountBookEventStorage, sponsorAccountStorage, currencyStorage, rewardStatusService);
    }

    @Bean
    public AccountingFacadePort unobservedAccountingService(final @NonNull AccountBookEventStorage accountBookEventStorage,
                                                            final @NonNull SponsorAccountStorage sponsorAccountStorage,
                                                            final @NonNull CurrencyStorage currencyStorage
    ) {
        return new AccountingService(accountBookEventStorage, sponsorAccountStorage, currencyStorage, null);
    }

    @Bean
    public AccountingObserverAdapter accountingObserverAdapter() {
        return new AccountingObserverAdapter();
    }

    @Bean
    public RewardStatusService rewardStatusService(final @NonNull RewardStatusStorage rewardStatusStorage,
                                                   final @NonNull AccountingFacadePort unobservedAccountingService) {
        return new RewardStatusService(rewardStatusStorage, unobservedAccountingService);
    }
}
