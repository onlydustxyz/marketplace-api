package com.onlydust.api.sumsub.api.client.adapter;

import org.apache.commons.codec.digest.HmacUtils;

public class SumsubSignatureVerifier {

    public static String hmac(final byte[] data, final String key) {
        return new HmacUtils("HmacSHA256", key).hmacHex(data);
    }
}
