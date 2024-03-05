package onlydust.com.marketplace.api.od.rust.api.client.adapter;

import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.out.OldRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.PayableRewardWithPayoutInfoView;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.MarkInvoiceAsReceivedDTO;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.RequestRewardDTO;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.RequestRewardResponseDTO;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.mapper.RewardMapper;
import onlydust.com.marketplace.project.domain.model.OldPayRewardRequestCommand;
import onlydust.com.marketplace.project.domain.model.OldRequestRewardCommand;
import onlydust.com.marketplace.project.domain.port.output.RewardServicePort;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class OdRustApiClientAdapter implements RewardServicePort, OldRewardStoragePort {

    private final OdRustApiHttpClient httpClient;

    @Override
    public UUID create(UUID requestorId, OldRequestRewardCommand oldRequestRewardCommand) {
        final RequestRewardDTO requestRewardDTO = RewardMapper.mapCreateRewardCommandToDTO(requestorId,
                oldRequestRewardCommand);
        final var response = httpClient.send("/api/payments", HttpMethod.POST, requestRewardDTO, RequestRewardResponseDTO.class);
        return response.map(RequestRewardResponseDTO::getPaymentId).orElse(null);
    }

    @Override
    public void cancel(UUID rewardId) {
        httpClient.send("/api/payments/" + rewardId.toString(), HttpMethod.DELETE, null, Void.class);
    }

    @Override
    public void markInvoiceAsReceived(List<UUID> rewardIds) {
        httpClient.send("/api/payments/invoiceReceivedAt", HttpMethod.PUT, new MarkInvoiceAsReceivedDTO(rewardIds), Void.class);
    }

    @Override
    public void markPaymentAsReceived(BigDecimal amount, OldPayRewardRequestCommand oldPayRewardRequestCommand) {
        httpClient.send("/api/payments/%s/receipts".formatted(oldPayRewardRequestCommand.getRewardId()), HttpMethod.POST,
                RewardMapper.mapOldPayRewardRequestCommandToDTO(oldPayRewardRequestCommand, amount), Void.class);
    }

    @Override
    public void markRewardAsPaid(PayableRewardWithPayoutInfoView payableRewardWithPayoutInfoView, String transactionHash) {
        httpClient.send("/api/payments/%s/receipts".formatted(payableRewardWithPayoutInfoView.id()), HttpMethod.POST,
                RewardMapper.mapPayableRewardWithPayoutInfoViewToDTO(payableRewardWithPayoutInfoView, transactionHash), Void.class);
    }
}
