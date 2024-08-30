package onlydust.com.marketplace.api.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookObserver;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookProjector;
import onlydust.com.marketplace.accounting.domain.port.in.*;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.*;
import onlydust.com.marketplace.api.infrastructure.aptosrpc.adapters.AptosAccountValidatorAdapter;
import onlydust.com.marketplace.api.infura.adapters.EthWeb3EnsValidatorAdapter;
import onlydust.com.marketplace.api.infura.adapters.StarknetAccountValidatorAdapter;
import onlydust.com.marketplace.api.infura.adapters.Web3EvmAccountAddressValidatorAdapter;
import onlydust.com.marketplace.api.read.adapters.ReadCurrencyApiPostgresAdapter;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.stellar.adapters.StellarAccountIdValidator;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper.SumsubMapper;
import onlydust.com.marketplace.kernel.port.output.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountingConfiguration {
    @Bean
    public CachedAccountBookProvider accountBookProvider(final @NonNull AccountBookEventStorage accountBookEventStorage,
                                                         final @NonNull AccountBookStorage accountBookStorage,
                                                         final @NonNull AccountBookObserver accountBookObserver
    ) {
        return new CachedAccountBookProvider(accountBookEventStorage, accountBookStorage, accountBookObserver);
    }

    @Bean
    public AccountingFacadePort accountingFacadePort(final @NonNull CachedAccountBookProvider cachedAccountBookProvider,
                                                     final @NonNull SponsorAccountStorage sponsorAccountStorage,
                                                     final @NonNull CurrencyStorage currencyStorage,
                                                     final @NonNull AccountingObserverPort accountingObserver,
                                                     final @NonNull ProjectAccountingObserver projectAccountingObserver,
                                                     final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                     final @NonNull RewardStatusService rewardStatusService,
                                                     final @NonNull ReceiptStoragePort receiptStoragePort,
                                                     final @NonNull BlockchainFacadePort blockchainFacadePort,
                                                     final @NonNull DepositStoragePort depositStoragePort,
                                                     final @NonNull PermissionPort permissionPort
    ) {
        return new AccountingService(cachedAccountBookProvider, sponsorAccountStorage, currencyStorage, accountingObserver, projectAccountingObserver,
                invoiceStoragePort, rewardStatusService, receiptStoragePort, blockchainFacadePort, depositStoragePort, permissionPort);
    }

    @Bean
    public AccountBookProjector accountBookProjector(final @NonNull AccountBookStorage accountBookStorage,
                                                     final @NonNull SponsorAccountStorage sponsorAccountStorage) {
        return new AccountBookProjector(accountBookStorage, sponsorAccountStorage);
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
    public AccountingNotifier accountingMailNotifier(final @NonNull BillingProfileStoragePort billingProfileStoragePort,
                                                     final @NonNull AccountingRewardStoragePort accountingRewardStoragePort,
                                                     final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                     final @NonNull NotificationPort notificationPort,
                                                     final @NonNull EmailStoragePort emailStoragePort) {
        return new AccountingNotifier(billingProfileStoragePort, accountingRewardStoragePort, invoiceStoragePort, notificationPort, emailStoragePort);
    }

    @Bean
    public BillingProfileFacadePort billingProfileFacadePort(final @NonNull InvoiceStoragePort invoiceStoragePort,
                                                             final @NonNull BillingProfileStoragePort billingProfileStoragePort,
                                                             final @NonNull PdfStoragePort pdfStoragePort,
                                                             final @NonNull BillingProfileObserverPort billingProfileObservers,
                                                             final @NonNull IndexerPort indexerPort,
                                                             final @NonNull AccountingObserverPort accountingObserver,
                                                             final @NonNull AccountingFacadePort accountingFacadePort,
                                                             final @NonNull PayoutInfoValidator payoutInfoValidator,
                                                             final @NonNull NotificationPort notificationPort
    ) {
        return new BillingProfileService(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort, billingProfileObservers,
                indexerPort, accountingObserver, accountingFacadePort, payoutInfoValidator, notificationPort);
    }

    @Bean
    public PayoutInfoValidator payoutInfoValidator(final @NonNull EthWeb3EnsValidatorAdapter ethereumEnsValidatorAdapter,
                                                   final @NonNull StarknetAccountValidatorAdapter starknetEnsValidatorAdapter,
                                                   final @NonNull Web3EvmAccountAddressValidatorAdapter infuraEvmAccountAddressValidatorAdapter,
                                                   final @NonNull AptosAccountValidatorAdapter aptosAccountValidatorAdapter,
                                                   final @NonNull StellarAccountIdValidator stellarAccountIdValidator) {
        return new PayoutInfoValidator(ethereumEnsValidatorAdapter, starknetEnsValidatorAdapter, infuraEvmAccountAddressValidatorAdapter,
                aptosAccountValidatorAdapter, stellarAccountIdValidator);
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
                                                     final AccountingNotifier accountingNotifier,
                                                     final AccountingTrackingNotifier accountingTrackingNotifier) {
        return new AccountingObserverComposite(accountingNotifier, rewardStatusUpdater, accountingTrackingNotifier);
    }

    @Bean
    public BillingProfileObserverPort billingProfileObservers(final RewardStatusUpdater rewardStatusUpdater,
                                                              final AccountingNotifier accountingNotifier,
                                                              final SlackApiAdapter slackApiAdapter) {
        return new BillingProfileObserverComposite(rewardStatusUpdater, accountingNotifier, slackApiAdapter);
    }

    @Bean
    public BillingProfileVerificationFacadePort billingProfileVerificationFacadePort(final OutboxPort billingProfileVerificationOutbox,
                                                                                     final BillingProfileStoragePort billingProfileStoragePort,
                                                                                     final BillingProfileVerificationProviderPort billingProfileVerificationProviderPort,
                                                                                     final BillingProfileObserverPort billingProfileObservers,
                                                                                     final SumsubMapper sumsubMapper) {
        return new BillingProfileVerificationService(billingProfileVerificationOutbox, sumsubMapper, billingProfileStoragePort,
                billingProfileVerificationProviderPort,
                billingProfileObservers);
    }

    @Bean
    public OutboxConsumer billingProfileVerificationOutboxConsumer(final OutboxPort billingProfileVerificationOutbox,
                                                                   final BillingProfileStoragePort billingProfileStoragePort,
                                                                   final BillingProfileVerificationProviderPort billingProfileVerificationProviderPort,
                                                                   final BillingProfileObserverPort billingProfileObservers,
                                                                   final SumsubMapper sumsubMapper
    ) {
        return new BillingProfileVerificationService(billingProfileVerificationOutbox,
                sumsubMapper,
                billingProfileStoragePort,
                billingProfileVerificationProviderPort,
                billingProfileObservers);
    }

    @Bean
    public SumsubMapper sumsubMapper(final BillingProfileVerificationRejectionReasonFacadePort billingProfileVerificationRejectionReasonFacadePort) {
        return new SumsubMapper(billingProfileVerificationRejectionReasonFacadePort);
    }

    @Bean
    public AccountingTrackingNotifier accountingTrackingNotifier(final OutboxPort trackingOutbox,
                                                                 final AccountingRewardStoragePort accountingRewardStoragePort) {
        return new AccountingTrackingNotifier(trackingOutbox, accountingRewardStoragePort);
    }

    @Bean
    public BillingProfileVerificationRejectionReasonFacadePort billingProfileVerificationRejectionReasonFacadePort(final BillingProfileStoragePort billingProfileStoragePort) {
        return new BillingProfileVerificationRejectionReasonService(billingProfileStoragePort);
    }

    @Bean
    @ConfigurationProperties(value = "application.onlydust-wallets", ignoreUnknownFields = false)
    public ReadCurrencyApiPostgresAdapter.OnlyDustWallets onlyDustWallets() {
        return new ReadCurrencyApiPostgresAdapter.OnlyDustWallets();
    }
}
