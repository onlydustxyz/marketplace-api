package onlydust.com.marketplace.api.sumsub.webhook.adapter;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.security.authentication.BadCredentialsException;

public class SumsubSignatureVerifier {

    public static void validateWebhook(final byte[] sumsubWebhookBodyBytes, final String webhookSecret,
                                       final String sumsub256Signature) {
        final String currentSha256Signature = hmac(sumsubWebhookBodyBytes, webhookSecret);
        if (!currentSha256Signature.equals(sumsub256Signature)) {
            throw OnlyDustException.badRequest("Invalid sha256 signature");
        }

    }

    public static String hmac(final byte[] data, final String key) {
        return new HmacUtils("HmacSHA256", key).hmacHex(data);
    }
}
