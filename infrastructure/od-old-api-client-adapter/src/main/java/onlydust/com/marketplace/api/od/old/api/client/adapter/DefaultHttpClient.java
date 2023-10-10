package onlydust.com.marketplace.api.od.old.api.client.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.od.old.api.client.adapter.dto.request.AllocateBudgetDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static onlydust.com.marketplace.api.domain.exception.OnlydustException.internalServerError;

@AllArgsConstructor
public class DefaultHttpClient implements HttpClientPort {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;


    private <ResponseBody> ResponseBody sendRequest(final HttpRequest httpRequest,
                                                    final Class<ResponseBody> responseClass) {
        try {
            final HttpResponse<byte[]> httpResponse = this.httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofByteArray());
            if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() <= 299) {
                return objectMapper.readValue(httpResponse.body(), responseClass);
            }
            throw OnlydustException.builder()
                    .message("INTERNAL_SERVER_ERROR")
                    .status(500)
                    .build();
        } catch (final InterruptedException | IOException e) {
            throw OnlydustException.builder()
                    .message("INTERNAL_SERVER_ERROR")
                    .status(500)
                    .rootException(e)
                    .build();
        }
    }


    @Override
    public UUID allocateBudget(UUID projectId, BigDecimal amount, String currency, UUID sponsorId) {
        final String url = String.format(baseUrl + "/api/v1/projects/%s/budgets", projectId);
        final AllocateBudgetDTO dto = AllocateBudgetDTO.builder()
                .amount(amount)
                .currency(currency)
                .sponsorId(sponsorId)
                .build();
        return post(url, dto, UUID.class);
    }

    private <Request, Response> Response post(String url, Request dto, Class<Response> responseClass) {
        return sendRequest(
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Api-Key", apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(
                                dtoToString(
                                        dto
                                )))
                        .build(),
                responseClass
        );
    }


    private <DTO> String dtoToString(DTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw internalServerError(e);
        }
    }
}
