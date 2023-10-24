package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.RewardItemRequest;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;

import java.util.UUID;

public interface RewardMapper {

    static RequestRewardCommand rewardRequestToDomain(final RewardRequest rewardRequest, final UUID projectId) {
        return RequestRewardCommand.builder()
                .amount(rewardRequest.getAmount())
                .projectId(projectId)
                .currency(switch (rewardRequest.getCurrency()) {
                    case OP -> Currency.Op;
                    case APT -> Currency.Apt;
                    case USD -> Currency.Usd;
                    case ETH -> Currency.Eth;
                    case STARK -> Currency.Stark;
                })
                .recipientId(rewardRequest.getRecipientId())
                .items(rewardRequest.getItems().stream().map(RewardMapper::rewardItemRequestToDomain).toList())
                .build();
    }

    private static RequestRewardCommand.Item rewardItemRequestToDomain(final RewardItemRequest rewardItemRequest) {
        return RequestRewardCommand.Item.builder()
                .id(rewardItemRequest.getId())
                .number(rewardItemRequest.getNumber())
                .repoId(rewardItemRequest.getRepoId())
                .type(switch (rewardItemRequest.getType()) {
                    case ISSUE -> RequestRewardCommand.Item.Type.issue;
                    case PULL_REQUEST -> RequestRewardCommand.Item.Type.pullRequest;
                    case CODE_REVIEW -> RequestRewardCommand.Item.Type.codeReview;
                })
                .build();
    }
}
