package onlydust.com.marketplace.api.sumsub.webhook.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper.SumsubWebhookSerdes;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Slf4j
@RestController
public class SumsubWebhookApiAdapter {

    public static final String X_OD_API = "X-OD-Api";
    public static final String X_SUMSUB_PAYLOAD_DIGEST = "x-payload-digest";
    private final SumsubProperties sumsubProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LegalVerificationFacadePort legalVerificationFacadePort;

    @PostMapping("/api/v1/sumsub/kyc/webhook")
    public ResponseEntity<Void> consumeKYCWebhook(final @RequestBody byte[] payload,
                                                  final @RequestHeader(X_OD_API) String odAPiHeader,
                                                  final @RequestHeader(X_SUMSUB_PAYLOAD_DIGEST) String signature) {
        if (!odAPiHeader.equals(sumsubProperties.getOdApiHeader())) {
            throw OnlyDustException.forbidden(String.format("Invalid sumsub header %s for value %s", X_OD_API, odAPiHeader));
        }
        SumsubSignatureVerifier.validateWebhook(payload, sumsubProperties.getSecret(), signature);
        legalVerificationFacadePort.update(SumsubWebhookSerdes.deserialize(payload, objectMapper));
        return ResponseEntity.ok().build();
    }
}
