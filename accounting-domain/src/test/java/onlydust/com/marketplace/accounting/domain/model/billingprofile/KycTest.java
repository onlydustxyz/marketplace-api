package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KycTest {
    @ParameterizedTest
    @CsvSource({
            ",,,",
            ",,FRA,",
            ",,USA,true",
            ",FRA,,",
            ",FRA,FRA,",
            ",FRA,USA,true",
            ",USA,,true",
            ",USA,FRA,true",
            ",USA,USA,true",
            "true,,,true",
            "true,,FRA,true",
            "true,,USA,true",
            "true,FRA,,true",
            "true,FRA,FRA,true",
            "true,FRA,USA,true",
            "true,USA,,true",
            "true,USA,FRA,true",
            "true,USA,USA,true",
            "false,,,",
            "false,,FRA,",
            "false,,USA,true",
            "false,FRA,,",
            "false,FRA,FRA,false",
            "false,FRA,USA,true",
            "false,USA,,true",
            "false,USA,FRA,true",
            "false,USA,USA,true",
    })
    void should_compute_us_citizen(Boolean usCitizen, String countryCode, String idDocumentCountryCode, Boolean expected) {
        // Given
        final var kyc = Kyc.builder()
                .id(UUID.randomUUID())
                .ownerId(UserId.random())
                .status(VerificationStatus.UNDER_REVIEW)
                .consideredUsPersonQuestionnaire(usCitizen)
                .country(countryCode == null ? null : Country.fromIso3(countryCode))
                .idDocumentCountry(idDocumentCountryCode == null ? null : Country.fromIso3(idDocumentCountryCode))
                .build();

        // When
        final var result = kyc.isUsCitizen();

        // Then
        assertEquals(expected, result);
    }
}