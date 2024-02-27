package com.onlydust.api.sumsub.api.client.adapter;

import com.onlydust.api.sumsub.api.client.adapter.dto.SumsubCompanyApplicantsDataDTO;
import com.onlydust.api.sumsub.api.client.adapter.dto.SumsubCompanyChecksDTO;
import com.onlydust.api.sumsub.api.client.adapter.dto.SumsubIndividualApplicantsDataDTO;
import com.onlydust.api.sumsub.api.client.adapter.mapper.SumsubResponseMapper;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileVerificationProviderPort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
public class SumsubApiClientAdapter implements BillingProfileVerificationProviderPort {

    public static final String X_APP_TOKEN = "X-App-Token";
    public static final String X_APP_ACCESS_TS = "X-App-Access-Ts";
    public static final String X_APP_ACCESS_SIG = "X-App-Access-Sig";
    private final SumsubClientProperties sumsubClientProperties;
    private final SumsubHttpClient sumsubHttpClient;
    private final SumsubResponseMapper sumsubResponseMapper = new SumsubResponseMapper();

    @Override
    public Kyc getUpdatedKyc(Kyc kyc) {
        final SumsubIndividualApplicantsDataDTO applicantsDataFromIndividualBillingProfileId =
                getApplicantsDataFromIndividualBillingProfileId(kyc.getId());
        return sumsubResponseMapper.updateKyc(applicantsDataFromIndividualBillingProfileId, kyc,
                sumsubClientProperties);
    }

    @Override
    public Kyb getUpdatedKyb(Kyb kyb) {
        final SumsubCompanyApplicantsDataDTO applicantsDataFromCompanyBillingProfileId =
                getApplicantsDataFromCompanyBillingProfileId(kyb.getId());
        final SumsubCompanyChecksDTO companyChecksFromSumsubApplicantId =
                getCompanyChecksFromSumsubApplicantId(applicantsDataFromCompanyBillingProfileId.getId());
        return sumsubResponseMapper.updateKyb(applicantsDataFromCompanyBillingProfileId, companyChecksFromSumsubApplicantId,
                kyb, sumsubClientProperties);
    }

    private SumsubIndividualApplicantsDataDTO getApplicantsDataFromIndividualBillingProfileId(final UUID billingProfileId) {
        final String now = Long.toString(Instant.now().getEpochSecond());
        final String method = "GET";
        final String path = String.format("/resources/applicants/-;externalUserId=%s/one", billingProfileId.toString());
        final String digest = SumsubSignatureVerifier.hmac((now + method + path).getBytes(StandardCharsets.UTF_8),
                sumsubClientProperties.getSecretKey());
        return sumsubHttpClient.send(path, HttpMethod.GET, null, SumsubIndividualApplicantsDataDTO.class, X_APP_TOKEN, sumsubClientProperties.getAppToken(),
                        X_APP_ACCESS_TS, now, X_APP_ACCESS_SIG, digest)
                .orElseThrow(() -> OnlyDustException.notFound(String.format("Applicants data not found on Sumsub for externalUserId = %s", billingProfileId)));
    }

    private SumsubCompanyApplicantsDataDTO getApplicantsDataFromCompanyBillingProfileId(final UUID billingProfileId) {
        final String now = Long.toString(Instant.now().getEpochSecond());
        final String method = "GET";
        final String path = String.format("/resources/applicants/-;externalUserId=%s/one", billingProfileId.toString());
        final String digest = SumsubSignatureVerifier.hmac((now + method + path).getBytes(StandardCharsets.UTF_8),
                sumsubClientProperties.getSecretKey());
        return sumsubHttpClient.send(path, HttpMethod.GET, null, SumsubCompanyApplicantsDataDTO.class, X_APP_TOKEN, sumsubClientProperties.getAppToken(),
                        X_APP_ACCESS_TS, now, X_APP_ACCESS_SIG, digest)
                .orElseThrow(() -> OnlyDustException.notFound(String.format("Applicants data not found on Sumsub for externalUserId = %s", billingProfileId)));
    }

    private SumsubCompanyChecksDTO getCompanyChecksFromSumsubApplicantId(final String applicantId) {
        final String now = Long.toString(Instant.now().getEpochSecond());
        final String method = "GET";
        final String path = String.format("/resources/checks/latest?type=COMPANY&applicantId=%s", applicantId);
        final String digest = SumsubSignatureVerifier.hmac((now + method + path).getBytes(StandardCharsets.UTF_8),
                sumsubClientProperties.getSecretKey());
        return sumsubHttpClient.send(path, HttpMethod.GET, null, SumsubCompanyChecksDTO.class, X_APP_TOKEN, sumsubClientProperties.getAppToken(),
                        X_APP_ACCESS_TS, now, X_APP_ACCESS_SIG, digest)
                .orElseThrow(() -> OnlyDustException.notFound(String.format("Applicants data not found on Sumsub for applicantId = %s", applicantId)));
    }

}
