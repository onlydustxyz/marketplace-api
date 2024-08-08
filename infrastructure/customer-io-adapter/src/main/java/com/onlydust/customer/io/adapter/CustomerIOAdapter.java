package com.onlydust.customer.io.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlydust.customer.io.adapter.client.CustomerIOHttpClient;
import com.onlydust.customer.io.adapter.dto.MailDTO;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.notification.*;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class CustomerIOAdapter implements NotificationSender {
    private final CustomerIOHttpClient customerIOHttpClient;
    private CustomerIOProperties customerIOProperties;

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void send(SendableNotification notification) {
        if (notification.data() instanceof BillingProfileVerificationFailed billingProfileVerificationFailed) {
            sendEmail(MailDTO.from(customerIOProperties, notification, billingProfileVerificationFailed));
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
        }
    }

    private <MessageData> void sendEmail(MailDTO<MessageData> mail) {
        try {
            System.out.printf(new ObjectMapper().writeValueAsString(mail));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        customerIOHttpClient.send("/send/email", HttpMethod.POST, mail, Void.class);
    }

    public static void main(String[] args) {
        final CustomerIOProperties properties = new CustomerIOProperties();
        properties.setBaseUri("https://api-eu.customer.io/v1");
        properties.setEnvironment("develop");
        properties.setOnlyDustAdminEmail("Camille from OnlyDust <admin@onlydust.xyz>");
        properties.setOnlyDustMarketingEmail("Emilie from OnlyDust <emilie@onlydust.xyz>");
        properties.setNewRewardReceivedEmailId(30);
        properties.setNewCommitteeApplicationEmailId(22);
        properties.setInvoiceRejectedEmailId(25);
        properties.setRewardCanceledEmailId(23);
        properties.setRewardsPaidEmailId(24);
        properties.setProjectApplicationAcceptedEmailId(27);
        final CustomerIOAdapter customerIOAdapter = new CustomerIOAdapter(new CustomerIOHttpClient(properties), properties);
        final NotificationRecipient pierreOucif = new NotificationRecipient(NotificationRecipient.Id.random(), "pierre@onlydust.xyz", "PierreOucif");
//        customerIOAdapter.send(SendableNotification.of(pierreOucif,
//                new Notification(Notification.Id.random(), UUID.randomUUID(), new CommitteeApplicationCreated(
//                        "projectName",
//                        UUID.randomUUID(),
//                        "committeeName",
//                        UUID.randomUUID(),
//                        ZonedDateTime.now()
//                ), ZonedDateTime.now(), Set.of())));

        final NotificationData notificationData = new RewardReceived(
                3,"PierreOucif", ShortReward.builder()
                .id(RewardId.random())
                .amount(BigDecimal.valueOf(111.2323))
                .currencyCode("STRK")
                .dollarsEquivalent(BigDecimal.valueOf(0.12323))
                .projectName("projectName")
                .build()
        );
        customerIOAdapter.send(SendableNotification.of(pierreOucif, new Notification(Notification.Id.random(), UUID.randomUUID(), notificationData,
                ZonedDateTime.now(), Set.of())));


    }
}
