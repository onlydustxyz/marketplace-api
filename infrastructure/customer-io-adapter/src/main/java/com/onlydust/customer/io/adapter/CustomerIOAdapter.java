package com.onlydust.customer.io.adapter;

import com.onlydust.customer.io.adapter.client.CustomerIOHttpClient;
import com.onlydust.customer.io.adapter.client.CustomerIOTrackingApiHttpClient;
import com.onlydust.customer.io.adapter.dto.MailDTO;
import com.onlydust.customer.io.adapter.dto.UpdateCustomerDTO;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfileChildrenKycVerification;
import onlydust.com.marketplace.accounting.domain.notification.*;
import onlydust.com.marketplace.accounting.domain.port.out.EmailStoragePort;
import onlydust.com.marketplace.project.domain.model.notification.*;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.output.MarketingNotificationSettingsStoragePort;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class CustomerIOAdapter implements NotificationSender, EmailStoragePort, MarketingNotificationSettingsStoragePort {
    private final CustomerIOHttpClient customerIOHttpClient;
    private final CustomerIOTrackingApiHttpClient customerIOTrackingApiHttpClient;
    private CustomerIOProperties customerIOProperties;

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void send(SendableNotification notification) {
        if (notification.data() instanceof BillingProfileVerificationClosed billingProfileVerificationClosed) {
            sendEmail(MailDTO.from(customerIOProperties, notification, billingProfileVerificationClosed));
        } else if (notification.data() instanceof CommitteeApplicationCreated committeeApplicationCreated) {
            sendEmail(MailDTO.from(customerIOProperties, notification, committeeApplicationCreated));
        } else if (notification.data() instanceof RewardReceived rewardReceived) {
            sendEmail(MailDTO.from(customerIOProperties, notification, rewardReceived));
        } else if (notification.data() instanceof RewardCanceled rewardCanceled) {
            sendEmail(MailDTO.from(customerIOProperties, notification, rewardCanceled));
        } else if (notification.data() instanceof RewardsPaid rewardsPaid) {
            sendEmail(MailDTO.from(customerIOProperties, notification, rewardsPaid));
        } else if (notification.data() instanceof InvoiceRejected invoiceRejected) {
            sendEmail(MailDTO.from(customerIOProperties, notification, invoiceRejected));
        } else if (notification.data() instanceof ApplicationAccepted applicationAccepted) {
            sendEmail(MailDTO.from(customerIOProperties, notification, applicationAccepted));
        } else if (notification.data() instanceof CompleteYourBillingProfile completeYourBillingProfile) {
            sendEmail(MailDTO.from(customerIOProperties, notification, completeYourBillingProfile));
        } else if (notification.data() instanceof BillingProfileVerificationRejected billingProfileVerificationRejected) {
            sendEmail(MailDTO.from(customerIOProperties, notification, billingProfileVerificationRejected));
        } else if (notification.data() instanceof BillingProfileVerificationClosed billingProfileVerificationClosed) {
            sendEmail(MailDTO.from(customerIOProperties, notification, billingProfileVerificationClosed));
        } else if (notification.data() instanceof ApplicationRefused applicationRefused) {
            sendEmail(MailDTO.from(customerIOProperties, notification, applicationRefused));
        } else if (notification.data() instanceof GoodFirstIssueCreated goodFirstIssueCreated) {
            sendEmail(MailDTO.from(customerIOProperties, notification, goodFirstIssueCreated));
        } else if (notification.data() instanceof FundsAllocatedToProgram fundsAllocatedToProgram) {
            sendEmail(MailDTO.from(customerIOProperties, notification, fundsAllocatedToProgram));
        } else if (notification.data() instanceof FundsUnallocatedFromProgram fundsUnallocatedFromProgram) {
            sendEmail(MailDTO.from(customerIOProperties, notification, fundsUnallocatedFromProgram));
        } else if (notification.data() instanceof FundsUngrantedFromProject fundsUngrantedFromProject) {
            sendEmail(MailDTO.from(customerIOProperties, notification, fundsUngrantedFromProject));
        } else if (notification.data() instanceof DepositRejected depositRejected) {
            sendEmail(MailDTO.from(customerIOProperties, notification, depositRejected));
        } else if (notification.data() instanceof DepositApproved depositApproved) {
            sendEmail(MailDTO.from(customerIOProperties, notification, depositApproved));
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void sendAllForRecipient(NotificationRecipient notificationRecipient, List<SendableNotification> sendableNotifications) {
        sendEmail(MailDTO.from(customerIOProperties, notificationRecipient, sendableNotifications));
    }

    private <MessageData> void sendEmail(MailDTO<MessageData> mail) {
        customerIOHttpClient.send("/send/email", HttpMethod.POST, mail, Void.class);
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void send(@NonNull String email, @NonNull Object object) {
        if (object instanceof BillingProfileChildrenKycVerification billingProfileChildrenKycVerification) {
            sendEmail(MailDTO.from(customerIOProperties, billingProfileChildrenKycVerification));
        } else {
            LOGGER.error("Cannot send email for unmanaged class %s".formatted(object.getClass().getSimpleName()));
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 200, multiplier = 2))
    public void update(String email, NotificationSettings settings) {
        customerIOTrackingApiHttpClient.send("/customers/%s".formatted(email), HttpMethod.PUT,
                UpdateCustomerDTO.fromTopicIdAndSubscription(customerIOProperties.getMarketingTopicId(), settings.hasSubscribedToMarketingEmailNotifications()),
                Void.class);
    }
}
