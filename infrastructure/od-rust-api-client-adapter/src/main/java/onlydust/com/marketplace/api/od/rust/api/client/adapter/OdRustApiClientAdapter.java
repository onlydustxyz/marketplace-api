package onlydust.com.marketplace.api.od.rust.api.client.adapter;

import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.port.output.RewardServicePort;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.MarkInvoiceAsReceivedDTO;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.RequestRewardDTO;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.RequestRewardResponseDTO;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.mapper.RewardMapper;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class OdRustApiClientAdapter implements RewardServicePort {

    private final OdRustApiHttpClient httpClient;

    @Override
    public UUID requestPayment(UUID requestorId, RequestRewardCommand requestRewardCommand) {
        final RequestRewardDTO requestRewardDTO = RewardMapper.mapCreateRewardCommandToDTO(requestorId,
                requestRewardCommand);
        final var response = httpClient.send("/api/payments", HttpMethod.POST, requestRewardDTO, RequestRewardResponseDTO.class);
        return response.map(RequestRewardResponseDTO::getPaymentId).orElse(null);
    }

    @Override
    public void cancelPayment(UUID rewardId) {
        httpClient.send("/api/payments/" + rewardId.toString(), HttpMethod.DELETE, null, Void.class);
    }

    @Override
    public void markInvoiceAsReceived(List<UUID> rewardIds) {
        httpClient.send("/api/payments/invoiceReceivedAt", HttpMethod.PUT, new MarkInvoiceAsReceivedDTO(rewardIds), Void.class);
    }
}
