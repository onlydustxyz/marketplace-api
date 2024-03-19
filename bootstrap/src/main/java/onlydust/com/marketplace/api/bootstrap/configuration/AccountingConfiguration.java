package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.observers.NotificationOutbox;
import onlydust.com.marketplace.accounting.domain.port.in.*;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.*;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper.SumsubMapper;
import onlydust.com.marketplace.kernel.port.output.*;
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
    public AccountingObserver accountingObserver(final @NonNull RewardStatusStorage rewardStatusStorage,
                                                 final @NonNull RewardUsdEquivalentStorage rewardUsdEquivalentStorage,
                                                 final @NonNull QuoteStorage quoteStorage,
                                                 final @NonNull CurrencyStorage currencyStorage,
                                                 final @NonNull InvoiceStoragePort invoiceStorage,
                                                 final @NonNull ReceiptStoragePort receiptStorage) {
        return new AccountingObserver(rewardStatusStorage, rewardUsdEquivalentStorage, quoteStorage, currencyStorage, invoiceStorage, receiptStorage);
    }

    @Bean
    public BillingProfileFacadePort billingProfileFacadePort(final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                             final @NonNull BillingProfileStoragePort billingProfileStoragePort,
                                                             final @NonNull PdfStoragePort pdfStoragePort,
                                                             final @NonNull BillingProfileObserver billingProfileObservers,
                                                             final @NonNull IndexerPort indexerPort,
                                                             final @NonNull AccountingObserverPort accountingObserverPort) {
        return new BillingProfileService(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort, billingProfileObservers, indexerPort,
                accountingObserverPort);
    }

    @Bean
    public NotificationOutbox notificationOutboxObserver(final @NonNull OutboxPort notificationOutbox) {
        return new NotificationOutbox(notificationOutbox);
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
    public BillingProfileObserverComposite billingProfileObservers(final @NonNull NotificationOutbox notificationOutbox,
                                                                   final @NonNull AccountingObserver accountingObserver) {
        return new BillingProfileObserverComposite(notificationOutbox, accountingObserver);
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
    public SponsorFacadePort sponsorFacadePort(final SponsorStoragePort sponsorStoragePort) {
        return new SponsorService(sponsorStoragePort);
    }
}
