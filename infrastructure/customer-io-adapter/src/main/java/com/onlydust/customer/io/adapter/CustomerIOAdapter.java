package com.onlydust.customer.io.adapter;

import com.onlydust.customer.io.adapter.client.CustomerIOHttpClient;
import com.onlydust.customer.io.adapter.dto.InvoiceRejectedDTO;
import com.onlydust.customer.io.adapter.dto.MailDTO;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.MailPort;

@AllArgsConstructor
public class CustomerIOAdapter implements MailPort {

    private final CustomerIOHttpClient customerIOHttpClient;

    @Override
    public void send(@NonNull Event event) {
        if (event instanceof InvoiceRejected invoiceRejected){
            customerIOHttpClient.send()
        }
    }
    
}
