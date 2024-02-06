package com.onlydust.api.sumsub.api.client.adapter;

import lombok.Data;

@Data
public class SumsubClientProperties {
    String baseUri;
    String appToken;
    String secretKey;
    String kycQuestionnaireName;
    String kybQuestionnaireName;
}
