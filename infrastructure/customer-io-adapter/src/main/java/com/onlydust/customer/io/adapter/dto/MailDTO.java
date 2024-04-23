package com.onlydust.customer.io.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;

import java.util.UUID;

@Builder
public record MailDTO<MessageData>(@NonNull @JsonProperty("transactional_message_id") String transactionalMessageId,
                                   @NonNull IdentifiersDTO identifiers,
                                   @NonNull String from,
                                   @NonNull String to,
                                   @NonNull String subject,
                                   @NonNull @JsonProperty("message_data") MessageData messageData
) {
    public record IdentifiersDTO(@NonNull String id) {
    }

    public MailDTO<InvoiceRejectedDTO> fromInvoiceRejected(@NonNull UUID toUserId,
                                                           @NonNull String to,
                                                           @NonNull String from,
                                                           @NonNull InvoiceRejected invoiceRejected) {
        return new MailDTO<>("1", new IdentifiersDTO(toUserId.toString()), from, to, subject,
                InvoiceRejectedDTO.fromEvent(invoiceRejected));
    }
}
