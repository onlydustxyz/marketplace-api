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
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.event.NewCommitteeApplication;

@AllArgsConstructor
@Slf4j
public class CustomerIOAdapter implements OutboxConsumer {

    private final CustomerIOHttpClient customerIOHttpClient;
    private CustomerIOProperties customerIOProperties;

    @Override
    public void process(@NonNull Event event) {
        if (event instanceof InvoiceRejected invoiceRejected) {
            sendEmail(MailDTO.fromInvoiceRejected(customerIOProperties, invoiceRejected));
        } else if (event instanceof RewardCreated rewardCreated) {
            sendEmail(MailDTO.fromRewardCreated(customerIOProperties, rewardCreated));
        } else if (event instanceof RewardCanceled rewardCanceled) {
            sendEmail(MailDTO.fromRewardCanceled(customerIOProperties, rewardCanceled));
        } else if (event instanceof BillingProfileVerificationFailed billingProfileVerificationFailed) {
            sendEmail(MailDTO.fromVerificationFailed(customerIOProperties, billingProfileVerificationFailed));
        } else if (event instanceof RewardsPaid rewardsPaid) {
            sendEmail(MailDTO.fromRewardsPaid(customerIOProperties, rewardsPaid));
        } else if (event instanceof NewCommitteeApplication newCommitteeApplication) {
            sendEmail(MailDTO.fromNewCommitteeApplication(customerIOProperties, newCommitteeApplication));
        } else {
            LOGGER.warn("Event type {} not handle by CustomerIO to send mail", event.getClass());
        }
    }

    private <MessageData> void sendEmail(MailDTO<MessageData> mail) {
        customerIOHttpClient.send("/send/email", HttpMethod.POST, mail, Void.class);
    }
}
