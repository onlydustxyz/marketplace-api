package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.in.BlockchainFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.PaymentPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingSponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.service.PaymentService;
import onlydust.com.marketplace.api.rest.api.adapter.BackofficeDebugRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.BackofficeProjectRestApi;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.project.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.project.domain.service.BackofficeService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackofficeConfiguration {
    @Bean
    public BackofficeFacadePort backofficeFacadePort(final BackofficeStoragePort backofficeStoragePort) {
        return new BackofficeService(backofficeStoragePort);
    }

    @Bean
    public AccountingRewardPort accountingRewardPort(final AccountingRewardStoragePort accountingRewardStoragePort,
                                                     final NotificationPort notificationPort,
                                                     final AccountingFacadePort accountingFacadePort,
                                                     final AccountingSponsorStoragePort accountingSponsorStoragePort) {
        return new onlydust.com.marketplace.accounting.domain.service.RewardService(accountingRewardStoragePort, accountingFacadePort,
                accountingSponsorStoragePort, notificationPort);
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
    @ConfigurationProperties(value = "application.web.debug")
    public BackofficeDebugRestApi.DebugProperties debugProperties() {
        return new BackofficeDebugRestApi.DebugProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "application.automated-rewards")
    public BackofficeProjectRestApi.OnlydustBotProperties onlydustBotProperties() {
        return new BackofficeProjectRestApi.OnlydustBotProperties();
    }
}

