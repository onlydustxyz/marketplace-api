package onlydust.com.marketplace.api.bootstrap.configuration;

import com.onlydust.customer.io.adapter.CustomerIOAdapter;
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
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper.SumsubMapper;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.jobs.RetriedOutboxConsumer;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
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
                                                     final @NonNull RewardStatusService rewardStatusService,
                                                     final @NonNull ReceiptStoragePort receiptStoragePort
    ) {
        return new AccountingService(cachedAccountBookProvider, sponsorAccountStorage, currencyStorage, accountingObserver, projectAccountingObserver,
                invoiceStoragePort, accountBookObserver, rewardStatusService, receiptStoragePort);
    }

    @Bean
    public AccountBookProjector accountBookProjector(final @NonNull SponsorAccountStorage sponsorAccountStorage) {
        return new AccountBookProjector(sponsorAccountStorage);
    }

    @Bean
    public RewardStatusService rewardStatusService(final @NonNull RewardStatusStorage rewardStatusStorage,
                                                   final @NonNull RewardUsdEquivalentStorage rewardUsdEquivalentStorage,
                                                   final @NonNull QuoteStorage quoteStorage,
                                                   final @NonNull CurrencyStorage currencyStorage) {
        return new RewardStatusService(rewardStatusStorage, rewardUsdEquivalentStorage, quoteStorage, currencyStorage);
    }

    @Bean
    public RewardStatusUpdater rewardStatusUpdater(final @NonNull RewardStatusFacadePort rewardStatusFacadePort,
                                                   final @NonNull RewardStatusStorage rewardStatusStorage,
                                                   final @NonNull InvoiceStoragePort invoiceStorage,
                                                   final @NonNull AccountingRewardStoragePort accountingRewardStoragePort) {
        return new RewardStatusUpdater(rewardStatusFacadePort, rewardStatusStorage, invoiceStorage, accountingRewardStoragePort);
    }

    @Bean
    public AccountingMailNotifier accountingMailNotifier(final @NonNull BillingProfileStoragePort billingProfileStoragePort,
                                                         final @NonNull AccountingRewardStoragePort accountingRewardStoragePort,
                                                         final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                         final @NonNull OutboxPort accountingMailOutbox) {
        return new AccountingMailNotifier(billingProfileStoragePort, accountingRewardStoragePort, invoiceStoragePort, accountingMailOutbox);
    }

    @Bean
    public BillingProfileFacadePort billingProfileFacadePort(final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                             final @NonNull BillingProfileStoragePort billingProfileStoragePort,
                                                             final @NonNull PdfStoragePort pdfStoragePort,
                                                             final @NonNull BillingProfileObserverPort billingProfileObservers,
                                                             final @NonNull IndexerPort indexerPort,
                                                             final @NonNull AccountingObserverPort accountingObserver,
                                                             final @NonNull AccountingFacadePort accountingFacadePort,
                                                             final @NonNull PayoutInfoValidator payoutInfoValidator
    ) {
        return new BillingProfileService(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort, billingProfileObservers,
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
                                               final @NonNull BillingProfileObserverPort billingProfileObservers,
                                               final @NonNull BillingProfileStoragePort billingProfileStoragePort
    ) {
        return new InvoiceService(invoiceStoragePort, pdfStoragePort, billingProfileStoragePort, billingProfileObservers);
    }

    @Bean
    public PayoutPreferenceFacadePort payoutPreferenceFacadePort(final PayoutPreferenceStoragePort payoutPreferenceStoragePort,
                                                                 final BillingProfileStoragePort billingProfileStoragePort,
                                                                 final AccountingObserverPort accountingObserver) {
        return new PayoutPreferenceService(payoutPreferenceStoragePort, billingProfileStoragePort, accountingObserver);
    }

    @Bean
    public AccountingObserverPort accountingObserver(final RewardStatusUpdater rewardStatusUpdater,
                                                     final AccountingMailNotifier accountingMailNotifier) {
        return new AccountingObserverComposite(accountingMailNotifier, rewardStatusUpdater);
    }

    @Bean
    public BillingProfileObserverPort billingProfileObservers(final RewardStatusUpdater rewardStatusUpdater,
                                                              final AccountingMailNotifier accountingMailNotifier,
                                                              final SlackApiAdapter slackApiAdapter) {
        return new BillingProfileObserverComposite(rewardStatusUpdater, accountingMailNotifier, slackApiAdapter);
    }

    @Bean
    public BillingProfileVerificationFacadePort billingProfileVerificationFacadePort(final OutboxPort billingProfileVerificationOutbox,
                                                                                     final BillingProfileStoragePort billingProfileStoragePort,
                                                                                     final BillingProfileVerificationProviderPort billingProfileVerificationProviderPort,
                                                                                     final BillingProfileObserverPort billingProfileObservers) {
        return new BillingProfileVerificationService(billingProfileVerificationOutbox, new SumsubMapper(), billingProfileStoragePort,
                billingProfileVerificationProviderPort,
                billingProfileObservers);
    }

    @Bean
    public OutboxConsumer billingProfileVerificationOutboxConsumer(final OutboxPort billingProfileVerificationOutbox,
                                                                   final BillingProfileStoragePort billingProfileStoragePort,
                                                                   final BillingProfileVerificationProviderPort billingProfileVerificationProviderPort,
                                                                   final BillingProfileObserverPort billingProfileObservers) {
        return new BillingProfileVerificationService(billingProfileVerificationOutbox, new SumsubMapper(), billingProfileStoragePort,
                billingProfileVerificationProviderPort,
                billingProfileObservers);
    }

    @Bean
    public SponsorFacadePort sponsorFacadePort(final SponsorStoragePort sponsorStoragePort, final ImageStoragePort imageStoragePort) {
        return new SponsorService(sponsorStoragePort, imageStoragePort);
    }

    @Bean
    public OutboxConsumerJob accountingMailOutboxJob(final PostgresOutboxAdapter<AccountingMailEventEntity> accountingMailOutbox,
                                                     final OutboxConsumer accountingMailOutboxConsumer) {
        return new OutboxConsumerJob(accountingMailOutbox, accountingMailOutboxConsumer);
    }

    @Bean
    public OutboxConsumer accountingMailOutboxConsumer(final CustomerIOAdapter customerIOAdapter) {
        return new RetriedOutboxConsumer(customerIOAdapter);
    }
}
