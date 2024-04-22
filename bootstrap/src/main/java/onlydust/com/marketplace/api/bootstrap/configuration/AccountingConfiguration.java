package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookObserver;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookProjector;
import onlydust.com.marketplace.accounting.domain.port.in.*;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.*;
import onlydust.com.marketplace.api.infura.adapters.EthInfuraEnsValidatorAdapter;
import onlydust.com.marketplace.api.infura.adapters.StarknetInfuraAccountValidatorAdapter;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper.SumsubMapper;
import onlydust.com.marketplace.kernel.port.output.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountingConfiguration {
    @Bean
    public CachedAccountBookProvider accountBookProvider(final @NonNull AccountBookEventStorage accountBookEventStorage) {
        return new CachedAccountBookProvider(accountBookEventStorage);
    }

    @Bean
    public AccountingFacadePort accountingFacadePort(final @NonNull CachedAccountBookProvider cachedAccountBookProvider,
                                                     final @NonNull SponsorAccountStorage sponsorAccountStorage,
                                                     final @NonNull CurrencyStorage currencyStorage,
                                                     final @NonNull AccountingObserver accountingObserver,
                                                     final @NonNull ProjectAccountingObserver projectAccountingObserver,
                                                     final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                     final @NonNull AccountBookObserver accountBookObserver
    ) {
        return new AccountingService(cachedAccountBookProvider, sponsorAccountStorage, currencyStorage, accountingObserver, projectAccountingObserver,
                invoiceStoragePort, accountBookObserver);
    }

    @Bean
    public AccountBookProjector accountBookProjector(final @NonNull SponsorAccountStorage sponsorAccountStorage) {
        return new AccountBookProjector(sponsorAccountStorage);
    }

    @Bean
    public AccountingObserver accountingObserver(final @NonNull RewardStatusStorage rewardStatusStorage,
                                                 final @NonNull RewardUsdEquivalentStorage rewardUsdEquivalentStorage,
                                                 final @NonNull QuoteStorage quoteStorage,
                                                 final @NonNull CurrencyStorage currencyStorage,
                                                 final @NonNull InvoiceStoragePort invoiceStorage,
                                                 final @NonNull ReceiptStoragePort receiptStorage,
                                                 final @NonNull BillingProfileStoragePort billingProfileStoragePort) {
        return new AccountingObserver(rewardStatusStorage, rewardUsdEquivalentStorage, quoteStorage, currencyStorage, invoiceStorage, receiptStorage,
                billingProfileStoragePort);
    }

    @Bean
    public BillingProfileFacadePort billingProfileFacadePort(final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                             final @NonNull BillingProfileStoragePort billingProfileStoragePort,
                                                             final @NonNull PdfStoragePort pdfStoragePort,
                                                             final @NonNull BillingProfileObserver billingProfileObservers,
                                                             final @NonNull IndexerPort indexerPort,
                                                             final @NonNull AccountingObserverPort accountingObserverPort,
                                                             final @NonNull AccountingFacadePort accountingFacadePort,
                                                             final @NonNull PayoutInfoValidator payoutInfoValidator
    ) {
        return new BillingProfileService(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort, billingProfileObservers,
                indexerPort, accountingObserverPort, accountingFacadePort, payoutInfoValidator);
    }

    @Bean
    public PayoutInfoValidator payoutInfoValidator(final @NonNull EthInfuraEnsValidatorAdapter ethereumEnsValidatorAdapter,
                                                   final @NonNull StarknetInfuraAccountValidatorAdapter starknetEnsValidatorAdapter) {
        return new PayoutInfoValidator(ethereumEnsValidatorAdapter, starknetEnsValidatorAdapter);
    }

    @Bean
    public InvoiceFacadePort invoiceFacadePort(final @NonNull InvoiceStoragePort invoiceStoragePort,
                                               final @NonNull PdfStoragePort pdfStoragePort,
                                               final @NonNull BillingProfileObserver billingProfileObservers,
                                               final @NonNull BillingProfileStoragePort billingProfileStoragePort
    ) {
        return new InvoiceService(invoiceStoragePort, pdfStoragePort, billingProfileStoragePort, billingProfileObservers);
    }

    @Bean
    public PayoutPreferenceFacadePort payoutPreferenceFacadePort(final PayoutPreferenceStoragePort payoutPreferenceStoragePort,
                                                                 final BillingProfileStoragePort billingProfileStoragePort,
                                                                 final AccountingObserverPort accountingObserverPort) {
        return new PayoutPreferenceService(payoutPreferenceStoragePort, billingProfileStoragePort, accountingObserverPort);
    }


    @Bean
    public BillingProfileVerificationFacadePort billingProfileVerificationFacadePort(final OutboxPort billingProfileVerificationOutbox,
                                                                                     final BillingProfileStoragePort billingProfileStoragePort,
                                                                                     final BillingProfileVerificationProviderPort billingProfileVerificationProviderPort,
                                                                                     final BillingProfileObserver billingProfileObservers,
                                                                                     final NotificationPort notificationPort,
                                                                                     final WebhookPort webhookNotificationPort) {
        return new BillingProfileVerificationService(billingProfileVerificationOutbox, new SumsubMapper(), billingProfileStoragePort,
                billingProfileVerificationProviderPort,
                billingProfileObservers, notificationPort, webhookNotificationPort);
    }

    @Bean
    public OutboxConsumer billingProfileVerificationOutboxConsumer(final OutboxPort billingProfileVerificationOutbox,
                                                                   final BillingProfileStoragePort billingProfileStoragePort,
                                                                   final BillingProfileVerificationProviderPort billingProfileVerificationProviderPort,
                                                                   final BillingProfileObserver billingProfileObservers,
                                                                   final NotificationPort notificationPort, final WebhookPort webhookNotificationPort) {
        return new BillingProfileVerificationService(billingProfileVerificationOutbox, new SumsubMapper(), billingProfileStoragePort,
                billingProfileVerificationProviderPort,
                billingProfileObservers, notificationPort, webhookNotificationPort);
    }

    @Bean
    public SponsorFacadePort sponsorFacadePort(final SponsorStoragePort sponsorStoragePort, final ImageStoragePort imageStoragePort) {
        return new SponsorService(sponsorStoragePort, imageStoragePort);
    }
}
