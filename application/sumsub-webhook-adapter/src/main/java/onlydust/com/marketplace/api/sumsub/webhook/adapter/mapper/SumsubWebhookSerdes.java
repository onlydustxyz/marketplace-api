package onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookDTO;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.io.IOException;

public interface SumsubWebhookSerdes {

    static SumsubWebhookDTO deserialize(final byte[] payload, final ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(payload, SumsubWebhookDTO.class);
        } catch (IOException e) {
            throw OnlyDustException.internalServerError("Failed to deserialize Sumsub webhook payload", e);
        }
    }

}
