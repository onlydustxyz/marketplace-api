package com.onlydust.api.sumsub.api.client.adapter;

import com.onlydust.api.sumsub.api.client.adapter.dto.SumsubApplicantsDataDTO;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.port.output.UserVerificationStoragePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
public class SumsubApiClientAdapter implements UserVerificationStoragePort {

    public static final String X_APP_TOKEN = "X-App-Token";
    public static final String X_APP_ACCESS_TS = "X-App-Access-Ts";
    public static final String X_APP_ACCESS_SIG = "X-App-Access-Sig";
    private final SumsubClientProperties sumsubClientProperties;
    private final SumsubHttpClient sumsubHttpClient;

    @Override
    public CompanyBillingProfile updateCompanyVerification(CompanyBillingProfile companyBillingProfile) {
        return null;
    }

    @Override
    public IndividualBillingProfile updateIndividualVerification(IndividualBillingProfile individualBillingProfile) {
        final SumsubApplicantsDataDTO applicantsDataFromBillingProfileId = getApplicantsDataFromBillingProfileId(individualBillingProfile.getId());
        return applicantsDataFromBillingProfileId
                .updateIndividualBillingProfile(individualBillingProfile, sumsubClientProperties);
    }

    private SumsubApplicantsDataDTO getApplicantsDataFromBillingProfileId(final UUID billingProfileId) {
        final String now = Long.toString(Instant.now().getEpochSecond());
        final String method = "GET";
        final String path = String.format("/resources/applicants/-;externalUserId=%s/one", billingProfileId.toString());
        final String digest = SumsubSignatureVerifier.hmac((now + method + path).getBytes(StandardCharsets.UTF_8),
                sumsubClientProperties.getSecretKey());
        return sumsubHttpClient.send(path, HttpMethod.GET, null, SumsubApplicantsDataDTO.class, X_APP_TOKEN, sumsubClientProperties.getAppToken(),
                        X_APP_ACCESS_TS, now, X_APP_ACCESS_SIG, digest)
                .orElseThrow(() -> OnlyDustException.notFound(String.format("Applicants data not found on Sumsub for externalUserId = %s", billingProfileId)));
    }

}
