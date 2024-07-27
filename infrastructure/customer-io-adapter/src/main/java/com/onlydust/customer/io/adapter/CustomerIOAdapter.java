package com.onlydust.customer.io.adapter;

import com.onlydust.customer.io.adapter.client.CustomerIOHttpClient;
import com.onlydust.customer.io.adapter.dto.MailDTO;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.*;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.kernel.model.notification.NotificationSender;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationsToReviewByUser;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationSuccessfullyCreated;

@AllArgsConstructor
@Slf4j
public class CustomerIOAdapter implements OutboxConsumer, NotificationSender {

    private final NotificationPort notificationPort;
    private final CustomerIOHttpClient customerIOHttpClient;
    private CustomerIOProperties customerIOProperties;

    @Override
    public void process(@NonNull Event event) {
        if (event instanceof InvoiceRejected invoiceRejected) {
            sendEmail(MailDTO.fromInvoiceRejected(customerIOProperties, invoiceRejected));
        } else if (event instanceof RewardCreatedMailEvent rewardCreated) {
            sendEmail(MailDTO.fromRewardCreated(customerIOProperties, rewardCreated));
        } else if (event instanceof RewardCanceled rewardCanceled) {
            sendEmail(MailDTO.fromRewardCanceled(customerIOProperties, rewardCanceled));
        } else if (event instanceof BillingProfileVerificationFailed billingProfileVerificationFailed) {
            sendEmail(MailDTO.fromVerificationFailed(customerIOProperties, billingProfileVerificationFailed));
        } else if (event instanceof RewardsPaid rewardsPaid) {
            sendEmail(MailDTO.fromRewardsPaid(customerIOProperties, rewardsPaid));
        } else if (event instanceof ProjectApplicationsToReviewByUser projectApplicationsToReviewByUser) {
            sendEmail(MailDTO.fromProjectApplicationsToReviewByUser(customerIOProperties, projectApplicationsToReviewByUser));
        } else if (event instanceof ProjectApplicationAccepted projectApplicationAccepted) {
            sendEmail(MailDTO.fromProjectApplicationAccepted(customerIOProperties, projectApplicationAccepted));
        } else {
            LOGGER.warn("Event type {} not handle by CustomerIO to send mail", event.getClass());
        }
    }

    @Override
    public void sendAll() {
        sendPendingEmails();
        sendPendingDailyEmails();
    }

    private void sendPendingEmails() {
        final var pendingNotificationsPerRecipient = notificationPort.getPendingNotificationsPerRecipient(NotificationChannel.EMAIL);
        pendingNotificationsPerRecipient.forEach((recipient, notifications) -> {
            notifications.forEach(notification -> {
                if (notification.data() instanceof CommitteeApplicationSuccessfullyCreated committeeApplicationSuccessfullyCreated) {
                    sendEmail(MailDTO.fromNewCommitteeApplication(customerIOProperties, recipient, committeeApplicationSuccessfullyCreated));
                }
            });
        });
    }

    private void sendPendingDailyEmails() {
        // TODO
    }


    private <MessageData> void sendEmail(MailDTO<MessageData> mail) {
        customerIOHttpClient.send("/send/email", HttpMethod.POST, mail, Void.class);
    }
}
