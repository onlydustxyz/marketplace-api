package com.onlydust.customer.io.adapter;

import com.onlydust.customer.io.adapter.client.CustomerIOHttpClient;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.*;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.MailPort;

@AllArgsConstructor
@Slf4j
public class CustomerIOAdapter implements MailPort {

    private final CustomerIOHttpClient customerIOHttpClient;

    @Override
    public void send(@NonNull Event event) {
        if (event instanceof InvoiceRejected invoiceRejected) {
//            customerIOHttpClient.send()
        } else if (event instanceof RewardCreated rewardCreated) {

        } else if (event instanceof RewardCanceled rewardCanceled) {

        } else if (event instanceof BillingProfileVerificationFailed billingProfileVerificationFailed) {

        } else if (event instanceof RewardsPaid rewardsPaid) {

        } else {
            LOGGER.warn("Event type {} not handle by CustomerIO to send mail", event.getClass());
        }
    }

}
