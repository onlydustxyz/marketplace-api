package com.onlydust.api.sumsub.api.client.adapter;

import com.onlydust.api.sumsub.api.client.adapter.dto.SumsubCompanyApplicantsDataDTO;
import com.onlydust.api.sumsub.api.client.adapter.dto.SumsubCompanyChecksDTO;
import com.onlydust.api.sumsub.api.client.adapter.dto.SumsubIndividualApplicantsDataDTO;
import com.onlydust.api.sumsub.api.client.adapter.mapper.SumsubResponseMapper;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.port.output.UserVerificationStoragePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.io.IOException;
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
    private final SumsubResponseMapper sumsubResponseMapper = new SumsubResponseMapper();

    @Override
    public CompanyBillingProfile updateCompanyVerification(CompanyBillingProfile companyBillingProfile) {
        final SumsubCompanyApplicantsDataDTO applicantsDataFromCompanyBillingProfileId =
                getApplicantsDataFromCompanyBillingProfileId(companyBillingProfile.getId());
        final SumsubCompanyChecksDTO companyChecksFromSumsubApplicantId =
                getCompanyChecksFromSumsubApplicantId(applicantsDataFromCompanyBillingProfileId.getId());
        return sumsubResponseMapper.updateCompanyBillingProfile(applicantsDataFromCompanyBillingProfileId, companyChecksFromSumsubApplicantId,
                companyBillingProfile, sumsubClientProperties);
    }

    @Override
    public IndividualBillingProfile updateIndividualVerification(IndividualBillingProfile individualBillingProfile) {
        final SumsubIndividualApplicantsDataDTO applicantsDataFromIndividualBillingProfileId =
                getApplicantsDataFromIndividualBillingProfileId(individualBillingProfile.getId());
        return sumsubResponseMapper.updateIndividualBillingProfile(applicantsDataFromIndividualBillingProfileId, individualBillingProfile,
                sumsubClientProperties);
    }

    private SumsubIndividualApplicantsDataDTO getApplicantsDataFromIndividualBillingProfileId(final UUID billingProfileId) {
        final String now = Long.toString(Instant.now().getEpochSecond());
        final String method = "GET";
        final String path = String.format("/resources/applicants/-;externalUserId=%s/one", billingProfileId.toString());
        final String digest = SumsubSignatureVerifier.hmac((now + method + path).getBytes(StandardCharsets.UTF_8),
                sumsubClientProperties.getSecretKey());
        System.out.println("Digest : %s".formatted(digest));
        System.out.println("Now : %s".formatted(now));
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


    public static void main(String[] args) throws IOException, InterruptedException {
        final SumsubClientProperties sumsubClientProperties = new SumsubClientProperties();
        sumsubClientProperties.setBaseUri("https://api.sumsub.com");
        sumsubClientProperties.setAppToken("sbx:Upa6XtQQfssOEBAAskudiZGN.u8YLvdDJZPiSnnKECIvqOjCR6t9QQi0q");
        sumsubClientProperties.setSecretKey("sU4tZo7AO3EAshbPcJKTiLRViU1a3lJG");
        sumsubClientProperties.setKybQuestionnaireName("od-kyb-staging");
        sumsubClientProperties.setKycQuestionnaireName("od-kyc-staging");
        final SumsubHttpClient sumsubHttpClient = new SumsubHttpClient(sumsubClientProperties);
        final SumsubApiClientAdapter sumsubApiClientAdapter = new SumsubApiClientAdapter(sumsubClientProperties, sumsubHttpClient);

        final SumsubIndividualApplicantsDataDTO applicantsDataFromIndividualBillingProfileId =
                sumsubApiClientAdapter.getApplicantsDataFromIndividualBillingProfileId(UUID.fromString("7182a960-3152-4888-9611-6a2d2d59d398"));

        System.out.printf(applicantsDataFromIndividualBillingProfileId.toString());
    }


}
