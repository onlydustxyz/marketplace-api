package onlydust.com.marketplace.api.sumsub.webhook.adapter;

import onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookDTO;

// Port to be moved into the domain when we will be ready to integrate the KYC and KYB
public interface LegalVerificationFacadePort {

    void update(final SumsubWebhookDTO sumsubWebhookDTO);
}
