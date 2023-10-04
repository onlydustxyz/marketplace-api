package com.onlydust.accounting.write.hexagon.usecases.rewarddemand;

import com.onlydust.accounting.write.adapters.secondary.repositories.LedgerRepositoryStub;
import com.onlydust.accounting.write.adapters.secondary.repositories.RewardDemandRepositoryStub;
import com.onlydust.shared.write.adapters.secondary.dateprovision.DeterministicDateProvider;
import com.onlydust.shared.write.adapters.secondary.repositories.DomainEventRepositoryStub;
import com.onlydust.accounting.write.hexagon.models.*;
import com.onlydust.shared.write.adapters.secondary.uuidgeneration.DeterministicUuidGenerator;
import com.onlydust.shared.write.hexagon.models.DomainEvent;
import com.onlydust.shared.write.hexagon.models.DomainEventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DemandRewardCommandHandlerTest {

    private final LedgerRepositoryStub ledgerRepository = new LedgerRepositoryStub();
    private final RewardDemandRepositoryStub rewardDemandRepository = new RewardDemandRepositoryStub();
    private final DomainEventRepositoryStub domainEventRepository = new DomainEventRepositoryStub();
    private final DeterministicUuidGenerator uuidGenerator = new DeterministicUuidGenerator();
    private final DeterministicDateProvider dateProvider = new DeterministicDateProvider();

    private final UUID aLedgerId = UUID.fromString("f7f6e5d4-c3b2-11eb-b8bc-0242ac130003");
    private final UUID aProjectId = UUID.fromString("a7f6e5d4-c3b2-11eb-b8bc-0242ac130003");
    private final UUID aRewardDemandId = UUID.fromString("d7f6e5d4-c3b2-11eb-b8bc-0242ac130003");
    private final LocalDateTime dateNow = LocalDateTime.of(2021, 6, 1, 0, 0);

    @BeforeEach
    public void setup() {
        uuidGenerator.setNextUuid(aRewardDemandId);
        dateProvider.setNow(dateNow);
        setExistingLedger(aLedgerId);
    }

    @Test
    void should_demand_a_reward_about_a_specific_ledger() {
        demandReward(new DemandRewardCommand(
                aLedgerId,
                BigDecimal.valueOf(120)
        ));

        assertExistingRewardsDemands(new RewardDemand(
                aRewardDemandId,
                aLedgerId,
                BigDecimal.valueOf(120))
        );

        assertSpawnedDomainEvents(new RewardDemandedEvent(
                aRewardDemandId,
                dateNow,
                new RewardDemandedEventPayload(aLedgerId, BigDecimal.valueOf(120)),
                DomainEventStatus.NEW
        ));
    }

    private void setExistingLedger(UUID ledgerId) {
        ledgerRepository.feedWith(new Ledger(
                ledgerId,
                aProjectId,
                "EUR"
        ));
    }


    private void demandReward(DemandRewardCommand demandRewardCommand) {
        new DemandRewardCommandHandler(
                rewardDemandRepository, ledgerRepository, domainEventRepository, uuidGenerator, dateProvider)
                .handle(demandRewardCommand);
    }

    private void assertExistingRewardsDemands(RewardDemand... expectedRewardDemands) {
        assertThat(rewardDemandRepository.rewardDemands()).containsExactly(expectedRewardDemands);
    }

    private void assertSpawnedDomainEvents(DomainEvent<?>... expectedDomainEvents) {
        assertThat(domainEventRepository.domainEvents()).containsExactly(expectedDomainEvents);
    }

}
