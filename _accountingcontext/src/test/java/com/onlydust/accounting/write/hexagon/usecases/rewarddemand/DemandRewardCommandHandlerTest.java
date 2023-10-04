package com.onlydust.accounting.write.hexagon.usecases.rewarddemand;

import com.onlydust.accounting.write.adapters.secondary.repositories.LedgerRepositoryStub;
import com.onlydust.accounting.write.adapters.secondary.repositories.RewardDemandRepositoryStub;
import com.onlydust.accounting.write.hexagon.models.*;
import com.onlydust.shared.write.adapters.secondary.dateprovision.DeterministicDateProvider;
import com.onlydust.shared.write.adapters.secondary.repositories.DomainEventRepositoryStub;
import com.onlydust.shared.write.adapters.secondary.uuidgeneration.DeterministicUuidGenerator;
import com.onlydust.shared.write.hexagon.models.DomainEvent;
import com.onlydust.shared.write.hexagon.models.DomainEventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DemandRewardCommandHandlerTest {

    private final LedgerRepositoryStub ledgerRepository = new LedgerRepositoryStub();
    private final RewardDemandRepositoryStub rewardDemandRepository = new RewardDemandRepositoryStub();
    private final DomainEventRepositoryStub domainEventRepository = new DomainEventRepositoryStub();
    private final DeterministicUuidGenerator uuidGenerator = new DeterministicUuidGenerator();
    private final DeterministicDateProvider dateProvider = new DeterministicDateProvider();

    private final LedgerId aLedgerId = new LedgerId("f7f6e5d4-c3b2-11eb-b8bc-0242ac130003");
    private final LedgerId anotherLedgerId = new LedgerId("bbbbe5d4-c3b2-11eb-b8bc-0242ac130003");
    private final ProjectId aProjectId = new ProjectId("a7f6e5d4-c3b2-11eb-b8bc-0242ac130003");
    private final RewardDemandId aRewardDemandId = new RewardDemandId("d7f6e5d4-c3b2-11eb-b8bc-0242ac130003");
    private final LocalDateTime dateNow = LocalDateTime.of(2021, 6, 1, 0, 0);

    @BeforeEach
    public void setup() {
        uuidGenerator.addNextUuid(aRewardDemandId.getId());
        dateProvider.setNow(dateNow);
        setExistingLedger(aLedgerId);
        setRemainingAllocation(aLedgerId, BigDecimal.valueOf(1000));
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
                dateNow,
                new RewardDemandedEventPayload(aLedgerId, aRewardDemandId, BigDecimal.valueOf(120)),
                DomainEventStatus.NEW
        ));
    }

    @Test
    void should_reject_a_reward_demand_when_not_enough_remaining_allocation() {
        assertThatThrownBy(() -> {
            demandReward(new DemandRewardCommand(
                    aLedgerId,
                    BigDecimal.valueOf(1200)
            ));
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough allocation to allow reward");


        assertExistingRewardsDemands();

        assertSpawnedDomainEvents();
    }

    @Test
    void should_reject_a_reward_demand_on_a_ledger_that_does_not_exist() {
        assertThatThrownBy(() -> {
            demandReward(new DemandRewardCommand(
                    anotherLedgerId,
                    BigDecimal.valueOf(120)
            ));
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ledger not found");


        assertExistingRewardsDemands();

        assertSpawnedDomainEvents();
    }

    private void setExistingLedger(LedgerId ledgerId) {
        ledgerRepository.save(new Ledger(
                ledgerId,
                aProjectId,
                "EUR"
        ));
    }

    private void setRemainingAllocation(LedgerId ledgerId, BigDecimal amount) {
        ledgerRepository.setRemainingAmount(
                ledgerId,
                amount
        );
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
