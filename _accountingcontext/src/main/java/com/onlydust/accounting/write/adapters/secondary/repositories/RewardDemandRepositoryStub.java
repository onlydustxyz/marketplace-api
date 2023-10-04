package com.onlydust.accounting.write.adapters.secondary.repositories;

import com.onlydust.accounting.write.hexagon.gateways.repositories.RewardDemandRepository;
import com.onlydust.accounting.write.hexagon.models.RewardDemand;

import java.util.ArrayList;
import java.util.List;

public class RewardDemandRepositoryStub implements RewardDemandRepository {

    private final List<RewardDemand> rewardDemands = new ArrayList<>();
    @Override
    public void save(RewardDemand rewardDemand) {
        rewardDemands.add(rewardDemand);
    }

    public List<RewardDemand> rewardDemands() {
        return rewardDemands;
    }
}
