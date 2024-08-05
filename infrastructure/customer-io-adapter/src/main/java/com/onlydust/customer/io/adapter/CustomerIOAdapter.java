package com.onlydust.customer.io.adapter;

import com.onlydust.customer.io.adapter.client.CustomerIOHttpClient;
import com.onlydust.customer.io.adapter.dto.MailDTO;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationFailed;
import onlydust.com.marketplace.accounting.domain.notification.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.notification.RewardCanceled;
import onlydust.com.marketplace.accounting.domain.notification.RewardReceived;
import onlydust.com.marketplace.accounting.domain.notification.RewardsPaid;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationsToReviewByUser;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@AllArgsConstructor
@Slf4j
public class CustomerIOAdapter implements OutboxConsumer, NotificationSender {

    private final CustomerIOHttpClient customerIOHttpClient;
    private CustomerIOProperties customerIOProperties;

    @Override
    public void process(@NonNull Event event) {
        if (event instanceof BillingProfileVerificationFailed billingProfileVerificationFailed) {
            sendEmail(MailDTO.fromVerificationFailed(customerIOProperties, billingProfileVerificationFailed));
        } else if (event instanceof ProjectApplicationsToReviewByUser projectApplicationsToReviewByUser) {
            sendEmail(MailDTO.fromProjectApplicationsToReviewByUser(customerIOProperties, projectApplicationsToReviewByUser));
        } else {
            LOGGER.warn("Event type {} not handle by CustomerIO to send mail", event.getClass());
        }
    }

    private <MessageData> void sendEmail(MailDTO<MessageData> mail) {
        customerIOHttpClient.send("/send/email", HttpMethod.POST, mail, Void.class);
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void send(SendableNotification notification) {
        if (notification.data() instanceof CommitteeApplicationCreated committeeApplicationCreated) {
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
        }
    }
}
