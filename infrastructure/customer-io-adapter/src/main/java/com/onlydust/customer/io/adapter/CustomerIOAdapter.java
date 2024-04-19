package com.onlydust.customer.io.adapter;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.MailPort;

public class CustomerIOAdapter implements MailPort {

    @Override
    public void send(@NonNull Event event) {

    }
}
