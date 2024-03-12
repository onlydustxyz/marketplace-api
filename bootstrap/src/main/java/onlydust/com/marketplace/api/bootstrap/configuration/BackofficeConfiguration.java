package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.accounting.domain.port.in.*;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import onlydust.com.marketplace.accounting.domain.port.out.OldRewardStoragePort;
import onlydust.com.marketplace.api.rest.api.adapter.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedBackofficeUserService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.token.QueryParamTokenAuthenticationService;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.project.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.project.domain.service.BackofficeService;
import onlydust.com.marketplace.project.domain.service.RewardService;
import onlydust.com.marketplace.project.domain.service.RewardV2Service;
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
                                                                                 final SponsorFacadePort sponsorFacadePort) {
        return new BackofficeSponsorManagementRestApi(backofficeFacadePort, sponsorFacadePort);
    }

    @Bean
    public BackofficeInvoicingManagementRestApi backofficeInvoicingManagementRestApi(final InvoiceFacadePort invoiceFacadePort,
                                                                                     final AccountingRewardPort accountingRewardPort,
                                                                                     final BillingProfileFacadePort billingProfileFacadePort,
                                                                                     final QueryParamTokenAuthenticationService.Config apiKeyAuthenticationConfig) {
        return new BackofficeInvoicingManagementRestApi(invoiceFacadePort, accountingRewardPort, billingProfileFacadePort, apiKeyAuthenticationConfig);
    }

    @Bean
    public BackofficeAccountingManagementRestApi backofficeAccountingManagementRestApi(
            final AccountingFacadePort accountingFacadePort,
            final RewardV2Service rewardV2Service,
            final CurrencyFacadePort currencyFacadePort,
            final UserFacadePort userFacadePort,
            final RewardService rewardService,
            final AccountingRewardPort accountingRewardPort) {
        return new BackofficeAccountingManagementRestApi(accountingFacadePort, rewardV2Service, currencyFacadePort, userFacadePort, rewardService,
                accountingRewardPort);
    }

    @Bean
    public BackofficeFacadePort backofficeFacadePort(final BackofficeStoragePort backofficeStoragePort) {
        return new BackofficeService(backofficeStoragePort);
    }

    @Bean
    public AccountingRewardPort accountingRewardPort(final AccountingRewardStoragePort accountingRewardStoragePort,
                                                     final OldRewardStoragePort oldRewardStoragePort,
                                                     final MailNotificationPort mailNotificationPort) {
        return new onlydust.com.marketplace.accounting.domain.service.RewardService(accountingRewardStoragePort, oldRewardStoragePort, mailNotificationPort);
    }
}
