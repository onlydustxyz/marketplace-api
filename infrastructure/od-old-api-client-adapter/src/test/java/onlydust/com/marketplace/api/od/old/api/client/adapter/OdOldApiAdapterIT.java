package onlydust.com.marketplace.api.od.old.api.client.adapter;

import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.api.od.old.api.client.adapter.dto.response.BudgetId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;

public class OdOldApiAdapterIT extends AbstractOdOldApiAdapter {

    @Autowired
    OdOldApiProperties odOldApiProperties;

    @Test
    void should_allocate_budget() throws IOException {
        // Given
        final BudgetId postAllocateBudget = getStubsFromClassT("post_allocate_budget", "response_ok.json", BudgetId.class);

        // When
        odOldApiAdapterWireMockServer.stubFor(WireMock.post(""))


//        githubAdapterWireMockServer.stubFor(
//                get(
//                        urlEqualTo("/app/installations"))
//                        .withHeader("Authorization",
//                                equalTo("Bearer " + githubJwtTokenProvider.getSignedJwtToken()))
//                        .willReturn(
//                                jsonResponse(
//                                        githubInstallationDTOS,
//                                        200)));

        // Then
    }
}
