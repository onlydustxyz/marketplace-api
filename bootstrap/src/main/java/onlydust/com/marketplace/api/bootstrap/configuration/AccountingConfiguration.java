package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookObserver;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookProjector;
import onlydust.com.marketplace.accounting.domain.port.in.*;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.*;
import onlydust.com.marketplace.api.infrastructure.aptosrpc.adapters.AptosAccountValidatorAdapter;
import onlydust.com.marketplace.api.infura.adapters.EthInfuraEnsValidatorAdapter;
import onlydust.com.marketplace.api.infura.adapters.InfuraEvmAccountAddressValidatorAdapter;
import onlydust.com.marketplace.api.infura.adapters.StarknetAccountValidatorAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresOutboxAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountingMailEventEntity;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper.SumsubMapper;
import onlydust.com.marketplace.kernel.jobs.NotificationOutboxConsumer;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.port.output.*;
import onlydust.com.marketplace.kernel.service.OutboxNotifier;
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
                                                     final @NonNull AccountingObserverPort accountingObserver,
                                                     final @NonNull ProjectAccountingObserver projectAccountingObserver,
                                                     final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                     final @NonNull AccountBookObserver accountBookObserver,
                                                     final @NonNull RewardStatusStorage rewardStatusStorage
    ) {
        return new AccountingService(cachedAccountBookProvider, sponsorAccountStorage, currencyStorage, accountingObserver, projectAccountingObserver,
                invoiceStoragePort, accountBookObserver, rewardStatusStorage);
    }

    @Bean
    public AccountBookProjector accountBookProjector(final @NonNull SponsorAccountStorage sponsorAccountStorage) {
        return new AccountBookProjector(sponsorAccountStorage);
    }

    @Bean
    public RewardStatusUpdater rewardStatusUpdater(final @NonNull RewardStatusStorage rewardStatusStorage,
                                                   final @NonNull RewardUsdEquivalentStorage rewardUsdEquivalentStorage,
                                                   final @NonNull QuoteStorage quoteStorage,
                                                   final @NonNull CurrencyStorage currencyStorage,
                                                   final @NonNull InvoiceStoragePort invoiceStorage,
                                                   final @NonNull ReceiptStoragePort receiptStorage,
                                                   final @NonNull AccountingRewardStoragePort accountingRewardStoragePort) {
        return new RewardStatusUpdater(rewardStatusStorage, rewardUsdEquivalentStorage, quoteStorage, currencyStorage, invoiceStorage, receiptStorage,
                accountingRewardStoragePort);
    }

    @Bean
    public AccountingNotifier accountingNotifier(final @NonNull BillingProfileStoragePort billingProfileStoragePort,
                                                 final @NonNull NotificationPort accountingMailOutboxNotifier,
                                                 final @NonNull AccountingRewardStoragePort accountingRewardStoragePort,
                                                 final @NonNull NotificationPort slackNotificationPort) {
        return new AccountingNotifier(billingProfileStoragePort, accountingRewardStoragePort, accountingMailOutboxNotifier, slackNotificationPort);
    }

    @Bean
    public BillingProfileFacadePort billingProfileFacadePort(final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                             final @NonNull BillingProfileStoragePort billingProfileStoragePort,
                                                             final @NonNull PdfStoragePort pdfStoragePort,
                                                             final @NonNull BillingProfileObserverPort billingProfileObserver,
                                                             final @NonNull IndexerPort indexerPort,
                                                             final @NonNull AccountingObserverPort accountingObserver,
                                                             final @NonNull AccountingFacadePort accountingFacadePort,
                                                             final @NonNull PayoutInfoValidator payoutInfoValidator
    ) {
        return new BillingProfileService(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort, billingProfileObserver,
                indexerPort, accountingObserver, accountingFacadePort, payoutInfoValidator);
    }

    @Bean
    public PayoutInfoValidator payoutInfoValidator(final @NonNull EthInfuraEnsValidatorAdapter ethereumEnsValidatorAdapter,
                                                   final @NonNull StarknetAccountValidatorAdapter starknetEnsValidatorAdapter,
                                                   final @NonNull InfuraEvmAccountAddressValidatorAdapter infuraEvmAccountAddressValidatorAdapter,
                                                   final @NonNull AptosAccountValidatorAdapter aptosAccountValidatorAdapter) {
        return new PayoutInfoValidator(ethereumEnsValidatorAdapter, starknetEnsValidatorAdapter, infuraEvmAccountAddressValidatorAdapter,
                aptosAccountValidatorAdapter);
    }

    @Bean
    public InvoiceFacadePort invoiceFacadePort(final @NonNull InvoiceStoragePort invoiceStoragePort,
                                               final @NonNull PdfStoragePort pdfStoragePort,
                                               final @NonNull BillingProfileObserverPort billingProfileObserver,
                                               final @NonNull BillingProfileStoragePort billingProfileStoragePort
    ) {
        return new InvoiceService(invoiceStoragePort, pdfStoragePort, billingProfileStoragePort, billingProfileObserver);
    }

    @Bean
    public PayoutPreferenceFacadePort payoutPreferenceFacadePort(final PayoutPreferenceStoragePort payoutPreferenceStoragePort,
                                                                 final BillingProfileStoragePort billingProfileStoragePort,
                                                                 final AccountingObserverPort accountingObserver) {
        return new PayoutPreferenceService(payoutPreferenceStoragePort, billingProfileStoragePort, accountingObserver);
    }

    @Bean
    public AccountingObserverPort accountingObserver(final RewardStatusUpdater rewardStatusUpdater,
                                                     final AccountingNotifier accountingNotifier) {
        return new AccountingObserverComposite(rewardStatusUpdater, accountingNotifier);
    }

    @Bean
    public BillingProfileObserverPort billingProfileObserver(final RewardStatusUpdater rewardStatusUpdater,
                                                             final AccountingNotifier accountingNotifier) {
        return new BillingProfileObserverComposite(rewardStatusUpdater, accountingNotifier);
    }

    @Bean
    public BillingProfileVerificationFacadePort billingProfileVerificationFacadePort(final OutboxPort billingProfileVerificationOutbox,
                                                                                     final BillingProfileStoragePort billingProfileStoragePort,
                                                                                     final BillingProfileVerificationProviderPort billingProfileVerificationProviderPort,
                                                                                     final BillingProfileObserverPort billingProfileObserver) {
        return new BillingProfileVerificationService(billingProfileVerificationOutbox, new SumsubMapper(), billingProfileStoragePort,
                billingProfileVerificationProviderPort,
                billingProfileObserver);
    }

    @Bean
    public OutboxConsumer billingProfileVerificationOutboxConsumer(final OutboxPort billingProfileVerificationOutbox,
                                                                   final BillingProfileStoragePort billingProfileStoragePort,
                                                                   final BillingProfileVerificationProviderPort billingProfileVerificationProviderPort,
                                                                   final BillingProfileObserverPort billingProfileObserver) {
        return new BillingProfileVerificationService(billingProfileVerificationOutbox, new SumsubMapper(), billingProfileStoragePort,
                billingProfileVerificationProviderPort,
                billingProfileObserver);
    }

    @Bean
    public SponsorFacadePort sponsorFacadePort(final SponsorStoragePort sponsorStoragePort, final ImageStoragePort imageStoragePort) {
        return new SponsorService(sponsorStoragePort, imageStoragePort);
    }

    @Bean
    public NotificationPort accountingMailOutboxNotifier(final OutboxPort accountingMailOutbox) {
        return new OutboxNotifier(accountingMailOutbox);
    }

    @Bean
    public OutboxConsumerJob accountingMailOutboxJob(final PostgresOutboxAdapter<AccountingMailEventEntity> accountingMailOutbox,
                                                     final OutboxConsumer accountingMailOutboxConsumer) {
        return new OutboxConsumerJob(accountingMailOutbox, accountingMailOutboxConsumer);
    }

    @Bean
    public OutboxConsumer accountingMailOutboxConsumer(final NotificationPort mailNotificationPort) {
        return new NotificationOutboxConsumer(mailNotificationPort);
    }
}
