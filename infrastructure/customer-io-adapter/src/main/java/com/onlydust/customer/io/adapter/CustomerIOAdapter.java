package com.onlydust.customer.io.adapter;

import com.onlydust.customer.io.adapter.client.CustomerIOHttpClient;
import com.onlydust.customer.io.adapter.dto.MailDTO;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfileChildrenKycVerification;
import onlydust.com.marketplace.accounting.domain.notification.*;
import onlydust.com.marketplace.accounting.domain.port.out.EmailStoragePort;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationRefused;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.project.domain.model.notification.GoodFirstIssueCreated;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class CustomerIOAdapter implements NotificationSender, EmailStoragePort {
    private final CustomerIOHttpClient customerIOHttpClient;
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
}
