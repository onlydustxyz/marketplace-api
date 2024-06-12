package onlydust.com.marketplace.api.posthog.processors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import onlydust.com.marketplace.accounting.domain.events.TrackingRewardCreated;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class TrackingRewardCreatedEventReader implements EventReader<TrackingRewardCreated> {
    @Override
    public void addProperties(final TrackingRewardCreated rewardCreated, final ObjectNode properties) {
        properties.put("amount", rewardCreated.amount().doubleValue());
        properties.put("count_contributions", rewardCreated.contributionsCount());
        properties.put("project_id", rewardCreated.projectId().toString());
        properties.put("sender_id", rewardCreated.senderId().toString());
        properties.put("currency", rewardCreated.currencyCode());
        properties.put("contributions_count", rewardCreated.contributionsCount());

        if (nonNull(rewardCreated.dollarsEquivalent()))
            properties.put("amount_in_dollars", rewardCreated.dollarsEquivalent().doubleValue());
    }

    @Override
    public String eventType(TrackingRewardCreated rewardCreated) {
        return "reward_received";
    }

    @Override
    public Object distinctId(TrackingRewardCreated rewardCreated) {
        return isNull(rewardCreated.recipientId()) ? UUID.randomUUID() : rewardCreated.recipientId();
    }

    @Override
    public Object timestamp(TrackingRewardCreated rewardCreated) {
        return Instant.now();
    }
}
