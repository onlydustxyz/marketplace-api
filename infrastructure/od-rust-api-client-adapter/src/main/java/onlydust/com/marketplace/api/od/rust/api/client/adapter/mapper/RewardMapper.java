package onlydust.com.marketplace.api.od.rust.api.client.adapter.mapper;

import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.MarkPaymentReceivedDTO;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.RequestRewardDTO;
import onlydust.com.marketplace.project.domain.model.OldPayRewardRequestCommand;
import onlydust.com.marketplace.project.domain.model.OldRequestRewardCommand;

import java.math.BigDecimal;
import java.util.UUID;

public interface RewardMapper {

    static RequestRewardDTO mapCreateRewardCommandToDTO(UUID requestorId, final OldRequestRewardCommand command) {
        return RequestRewardDTO.builder()
                .amount(command.getAmount())
                .currency(switch (command.getCurrency()) {
                    case ETH -> "ETH";
                    case OP -> "OP";
                    case APT -> "APT";
                    case USD -> "USD";
                    case STRK -> "STRK";
                    case LORDS -> "LORDS";
                    case USDC -> "USDC";
                })
                .projectId(command.getProjectId())
                .recipientId(command.getRecipientId())
                .requestorId(requestorId)
                .reason(RequestRewardDTO.ReasonDTO.builder()
                        .workItems(
                                command.getItems().stream().map(
                                        RewardMapper::itemToDTO
                                ).toList()
                        )
                        .build())
                .build();
    }

    private static RequestRewardDTO.ReasonDTO.WorkItemDTO itemToDTO(final OldRequestRewardCommand.Item item) {
        return RequestRewardDTO.ReasonDTO.WorkItemDTO.builder()
                .id(item.getId())
                .number(item.getNumber())
                .repoId(item.getRepoId())
                .type(switch (item.getType()) {
                    case issue -> "ISSUE";
                    case codeReview -> "CODE_REVIEW";
                    case pullRequest -> "PULL_REQUEST";
                })
                .build();
    }

    static MarkPaymentReceivedDTO mapOldPayRewardRequestCommandToDTO(final OldPayRewardRequestCommand command,
                                                                             final BigDecimal amount) {
        return MarkPaymentReceivedDTO.builder()
                .amount(amount)
                .transactionReference(command.getTransactionReference())
                .recipientWallet(switch (command.getCurrency()) {
                    case STRK, ETH, OP, LORDS, APT, USDC:
                        yield command.getRecipientAccount();
                    default:
                        yield null;
                })
                .recipientIban(switch (command.getCurrency()) {
                    case USD:
                        yield command.getRecipientAccount();
                    default:
                        yield null;
                })
                .currency(switch (command.getCurrency()) {
                    case ETH -> "ETH";
                    case OP -> "OP";
                    case APT -> "APT";
                    case USD -> "USD";
                    case STRK -> "STRK";
                    case LORDS -> "LORDS";
                    case USDC -> "USDC";
                })
                .build();
    }
}
