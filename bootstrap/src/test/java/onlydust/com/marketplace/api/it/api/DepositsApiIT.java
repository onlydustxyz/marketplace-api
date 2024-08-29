package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@TagAccounting
public class DepositsApiIT extends AbstractMarketplaceApiIT {
    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setUp() {
        caller = userAuthHelper.create();
    }

    @Nested
    class GivenMySponsor {
        Sponsor sponsor;

        @BeforeEach
        void setUp() {
            sponsor = sponsorHelper.create(caller);
        }

        @Test
        void should_be_preview_a_deposit_on_ethereum() {
            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "ETHEREUM",
                                "transactionReference": "0x1234567890123456789012345678901234567890"
                            }
                            """)
                    .exchange()
                    // Then
                    .expectStatus()
                    .isBadRequest(); // FIXME
        }
    }

    @Nested
    class GivenNotMySponsor {
        Sponsor sponsor;

        @BeforeEach
        void setUp() {
            sponsor = sponsorHelper.create();
        }

        @Test
        void should_be_unauthorized_previewing_a_deposit() {
            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "ETHEREUM",
                                "transactionReference": "0x1234567890123456789012345678901234567890"
                            }
                            """)
                    .exchange()
                    // Then
                    .expectStatus()
                    .isForbidden();
        }
    }
}
