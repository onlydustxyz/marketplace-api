package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.InvoiceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.accounting.domain.service.InvoiceService;
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
                                                     final @NonNull RewardStatusService rewardStatusService,
                                                     final @NonNull ProjectAccountingObserver projectAccountingObserver
    ) {
        return new AccountingService(accountBookEventStorage, sponsorAccountStorage, currencyStorage, rewardStatusService, projectAccountingObserver);
    }

    @Bean
    public AccountingObserverAdapter accountingObserverAdapter(
            final @NonNull RewardStatusStorage rewardStatusStorage,
            final @NonNull RewardStatusFacadePort rewardStatusFacadePort
    ) {
        return new AccountingObserverAdapter(rewardStatusStorage, rewardStatusFacadePort);
    }

    @Bean
    public RewardStatusService rewardStatusService(final @NonNull RewardStatusStorage rewardStatusStorage,
                                                   final @NonNull RewardUsdEquivalentStorage rewardUsdEquivalentStorage,
                                                   final @NonNull QuoteStorage quoteStorage,
                                                   final @NonNull CurrencyStorage currencyStorage) {
        return new RewardStatusService(rewardStatusStorage, rewardUsdEquivalentStorage, quoteStorage, currencyStorage);
    }

    @Bean
    public BillingProfileFacadePort billingProfileFacadePort(final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                             final @NonNull BillingProfileStorage billingProfileStorage,
                                                             final @NonNull PdfStoragePort pdfStoragePort) {
        return new BillingProfileService(invoiceStoragePort, billingProfileStorage, pdfStoragePort);
    }

    @Bean
    public InvoiceFacadePort invoiceFacadePort(final @NonNull InvoiceStoragePort invoiceStoragePort) {
        return new InvoiceService(invoiceStoragePort);
    }
}
