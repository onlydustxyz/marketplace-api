package com.onlydust.api.sumsub.api.client.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualKycIdentity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SumsubIndividualApplicantsByApplicantIdDataDTO {
    String email;
    FixedInfoDTO fixedInfo;

    public IndividualKycIdentity toIndividualKycIdentity() {
        return new IndividualKycIdentity(email, fixedInfo.firstName, fixedInfo.lastName);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FixedInfoDTO {
        String firstName;
        String lastName;
    }
}
