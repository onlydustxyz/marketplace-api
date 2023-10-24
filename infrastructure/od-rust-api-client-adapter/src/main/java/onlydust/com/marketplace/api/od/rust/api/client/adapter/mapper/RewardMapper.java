package onlydust.com.marketplace.api.od.rust.api.client.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.RequestRewardDTO;

public interface RewardMapper {

    static RequestRewardDTO mapCreateRewardCommandToDTO(final RequestRewardCommand command) {
        return RequestRewardDTO.builder()
                .amount(command.getAmount())
                .currency(switch (command.getCurrency()) {
                    case Eth -> "ETH";
                    case Op -> "OP";
                    case Apt -> "APT";
                    case Usd -> "USD";
                    case Stark -> "STARK";
                })
                .projectId(command.getProjectId())
                .recipientId(command.getRecipientId())
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
