package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.accounting.domain.port.in.*;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.service.PaymentService;
import onlydust.com.marketplace.api.rest.api.adapter.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedBackofficeUserService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.token.QueryParamTokenAuthenticationService;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.project.domain.port.input.HackathonFacadePort;
import onlydust.com.marketplace.project.domain.port.input.LanguageFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.project.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.LanguageStorage;
import onlydust.com.marketplace.project.domain.service.BackofficeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("bo")
public class BackofficeConfiguration {

    @Bean
    public BackofficeRestApi backofficeRestApi(final BackofficeFacadePort backofficeFacadePort) {
        return new BackofficeRestApi(backofficeFacadePort);
    }

    @Bean
    public BackofficeMeRestApi backofficeMeRestApi(final AuthenticatedBackofficeUserService authenticatedBackofficeUserService) {
        return new BackofficeMeRestApi(authenticatedBackofficeUserService);
    }

    @Bean
    public BackofficeCurrencyManagementRestApi backofficeCurrencyManagementRestApi(final CurrencyFacadePort currencyFacadePort) {
        return new BackofficeCurrencyManagementRestApi(currencyFacadePort);
    }

    @Bean
    public BackofficeSponsorManagementRestApi backofficeSponsorManagementRestApi(final BackofficeFacadePort backofficeFacadePort,
                                                                                 final SponsorFacadePort sponsorFacadePort,
                                                                                 final AccountingFacadePort accountingFacadePort) {
        return new BackofficeSponsorManagementRestApi(backofficeFacadePort, sponsorFacadePort, accountingFacadePort);
    }

    @Bean
    public BackofficeInvoicingManagementRestApi backofficeInvoicingManagementRestApi(final InvoiceFacadePort invoiceFacadePort,
                                                                                     final QueryParamTokenAuthenticationService.Config apiKeyAuthenticationConfig,
                                                                                     final AuthenticatedBackofficeUserService authenticatedBackofficeUserService) {
        return new BackofficeInvoicingManagementRestApi(invoiceFacadePort, apiKeyAuthenticationConfig, authenticatedBackofficeUserService);
    }

    @Bean
    public BackofficeAccountingManagementRestApi backofficeAccountingManagementRestApi(
            final AccountingFacadePort accountingFacadePort,
            final AccountingRewardPort accountingRewardPort,
            final PaymentPort paymentPort,
            final BillingProfileFacadePort billingProfileFacadePort,
            final AuthenticatedBackofficeUserService authenticatedBackofficeUserService,
            final BlockchainFacadePort blockchainFacadePort
    ) {
        return new BackofficeAccountingManagementRestApi(accountingFacadePort, accountingRewardPort,
                paymentPort, billingProfileFacadePort, authenticatedBackofficeUserService, blockchainFacadePort);
    }

    @Bean
    public BackofficeFacadePort backofficeFacadePort(final BackofficeStoragePort backofficeStoragePort) {
        return new BackofficeService(backofficeStoragePort);
    }

    @Bean
    public AccountingRewardPort accountingRewardPort(final AccountingRewardStoragePort accountingRewardStoragePort,
                                                     final OutboxConsumer accountingMailOutboxConsumer,
                                                     final AccountingFacadePort accountingFacadePort,
                                                     final SponsorStoragePort sponsorStoragePort) {
        return new onlydust.com.marketplace.accounting.domain.service.RewardService(accountingRewardStoragePort, accountingFacadePort,
                sponsorStoragePort, accountingMailOutboxConsumer);
    }

    @Bean
    public PaymentPort batchPaymentPort(final AccountingRewardStoragePort accountingRewardStoragePort,
                                        final InvoiceStoragePort invoiceStoragePort,
                                        final AccountingFacadePort accountingFacadePort,
                                        final BlockchainFacadePort blockchainFacadePort) {
        return new PaymentService(accountingRewardStoragePort, invoiceStoragePort,
                accountingFacadePort, blockchainFacadePort);
    }

    @Bean
    public BackofficeHackathonRestApi backofficeHackathonApi(final HackathonFacadePort hackathonFacadePort) {
        return new BackofficeHackathonRestApi(hackathonFacadePort);
    }

    @Bean
    public BackofficeDebugRestApi backofficeDebugRestApi(final AccountBookEventStorage accountBookEventStorage,
                                                         final CurrencyFacadePort currencyFacadePort) {
        return new BackofficeDebugRestApi(accountBookEventStorage, currencyFacadePort);
    }

    @Bean
    public BackofficeUserRestApi backofficeUserRestApi(final BackofficeFacadePort backofficeFacadePort,
                                                       final UserFacadePort userFacadePort,
                                                       final BillingProfileFacadePort billingProfileFacadePort) {
        return new BackofficeUserRestApi(backofficeFacadePort, userFacadePort, billingProfileFacadePort);
    }

    @Bean
    public BackofficeLanguageRestApi backofficeLanguageRestApi(final LanguageFacadePort languageFacadePort) {
        return new BackofficeLanguageRestApi(languageFacadePort);
    }

    @Bean
    public LanguageFacadePort languageFacadePort(final LanguageStorage languageStorage,
                                                 final ImageStoragePort imageStoragePort) {
        return new onlydust.com.marketplace.project.domain.service.LanguageService(languageStorage, imageStoragePort);
    }
}

