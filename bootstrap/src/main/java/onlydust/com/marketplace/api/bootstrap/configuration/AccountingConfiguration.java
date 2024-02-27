package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.observers.NotificationOutbox;
import onlydust.com.marketplace.accounting.domain.port.in.*;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.*;
import onlydust.com.marketplace.api.infrastructure.accounting.AccountingObserverAdapter;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountingConfiguration {
    @Bean
    public AccountingFacadePort accountingFacadePort(final @NonNull AccountBookEventStorage accountBookEventStorage,
                                                     final @NonNull SponsorAccountStorage sponsorAccountStorage,
                                                     final @NonNull CurrencyStorage currencyStorage,
                                                     final @NonNull AccountingObserver accountingObserver,
                                                     final @NonNull ProjectAccountingObserver projectAccountingObserver
    ) {
        return new AccountingService(accountBookEventStorage, sponsorAccountStorage, currencyStorage, accountingObserver, projectAccountingObserver);
    }

    @Bean
    public AccountingObserverAdapter accountingObserverAdapter(
            final @NonNull RewardStatusStorage rewardStatusStorage,
            final @NonNull RewardStatusFacadePort rewardStatusFacadePort
    ) {
        return new AccountingObserverAdapter(rewardStatusStorage, rewardStatusFacadePort);
    }

    @Bean
    public AccountingObserver accountingObserver(final @NonNull RewardStatusStorage rewardStatusStorage,
                                                 final @NonNull RewardUsdEquivalentStorage rewardUsdEquivalentStorage,
                                                 final @NonNull QuoteStorage quoteStorage,
                                                 final @NonNull CurrencyStorage currencyStorage,
                                                 final @NonNull InvoiceStoragePort invoiceStorage) {
        return new AccountingObserver(rewardStatusStorage, rewardUsdEquivalentStorage, quoteStorage, currencyStorage, invoiceStorage);
    }

    @Bean
    public BillingProfileFacadePort billingProfileFacadePort(final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                             final @NonNull BillingProfileStoragePort billingProfileStoragePort,
                                                             final @NonNull PdfStoragePort pdfStoragePort,
                                                             final @NonNull BillingProfileObserver billingProfileObserver,
                                                             final @NonNull IndexerPort indexerPort) {
        return new BillingProfileService(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort, billingProfileObserver, indexerPort);
    }

    @Bean
    public NotificationOutbox notificationOutboxObserver(final @NonNull OutboxPort notificationOutbox) {
        return new NotificationOutbox(notificationOutbox);
    }

    @Bean
    public InvoiceFacadePort invoiceFacadePort(final @NonNull InvoiceStoragePort invoiceStoragePort, final @NonNull PdfStoragePort pdfStoragePort) {
        return new InvoiceService(invoiceStoragePort, pdfStoragePort);
    }

    @Bean
    public PayoutPreferenceFacadePort payoutPreferenceFacadePort(final PayoutPreferenceStoragePort payoutPreferenceStoragePort,
                                                                 final BillingProfileStoragePort billingProfileStoragePort) {
        return new PayoutPreferenceService(payoutPreferenceStoragePort, billingProfileStoragePort);
    }
}
