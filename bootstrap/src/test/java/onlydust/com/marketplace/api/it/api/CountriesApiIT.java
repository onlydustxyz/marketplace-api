package onlydust.com.marketplace.api.it.api;


import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

public class CountriesApiIT extends AbstractMarketplaceApiIT {
    UserAuthHelper.AuthenticatedUser caller;
    static AtomicBoolean setupDone = new AtomicBoolean(false);

    @BeforeEach
    synchronized void setUp() {
        caller = userAuthHelper.authenticateOlivier();
        if (setupDone.compareAndExchange(false, true)) return;

        billingProfileHelper.verify(userAuthHelper.create(), Country.fromIso3("FRA"));
        billingProfileHelper.verify(userAuthHelper.create(), Country.fromIso3("USA"));
        billingProfileHelper.verify(userAuthHelper.create(), Country.fromIso3("GBR"));
    }

    @Test
    void should_get_all_countries() {
        // When
        client.get()
                .uri(getApiURI(COUNTRIES))
                .header("Authorization", BEARER_PREFIX + caller.jwt())
                // Then
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "countries": [
                            {
                              "code": "FR"
                            },
                            {
                              "code": "GB"
                            },
                            {
                              "code": "US"
                            }
                          ]
                        }
                        """, true);
    }

    @Test
    void should_search_country_by_name() {
        // When
        client.get()
                .uri(getApiURI(COUNTRIES, Map.of("search", "france")))
                .header("Authorization", BEARER_PREFIX + caller.jwt())
                // Then
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "countries": [
                            {
                              "code": "FR"
                            }
                          ]
                        }
                        """);
    }
}
