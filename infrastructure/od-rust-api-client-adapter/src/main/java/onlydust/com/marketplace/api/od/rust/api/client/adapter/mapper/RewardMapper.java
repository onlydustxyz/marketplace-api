package onlydust.com.marketplace.api.od.rust.api.client.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.RequestRewardDTO;

import java.util.UUID;

public interface RewardMapper {

    static RequestRewardDTO mapCreateRewardCommandToDTO(UUID requestorId, final RequestRewardCommand command) {
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

    private static RequestRewardDTO.ReasonDTO.WorkItemDTO itemToDTO(final RequestRewardCommand.Item item) {
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
}
