package onlydust.com.marketplace.api.od.rust.api.client.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.port.output.RewardServicePort;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.RequestRewardDTO;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.RequestRewardResponseDTO;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.mapper.RewardMapper;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraAuthentication;
import org.springframework.http.HttpMethod;

import java.util.UUID;

@AllArgsConstructor
public class OdRustApiClientAdapter implements RewardServicePort<HasuraAuthentication> {

    private final OdRustApiHttpClient httpClient;

    @Override
    public UUID requestPayment(final HasuraAuthentication authentication, RequestRewardCommand requestRewardCommand) {
        final RequestRewardDTO requestRewardDTO = RewardMapper.mapCreateRewardCommandToDTO(requestRewardCommand);
        final var response = httpClient.sendRequest("/api/payments", HttpMethod.POST, requestRewardDTO,
                RequestRewardResponseDTO.class,
                authentication);
        return response.getPaymentId();
    }

    @Override
    public void cancelPayment(HasuraAuthentication authentication, UUID rewardId) {
        httpClient.sendRequest("/api/payments/" + rewardId.toString(), HttpMethod.DELETE, null, Void.class,
                authentication);
    }
}
