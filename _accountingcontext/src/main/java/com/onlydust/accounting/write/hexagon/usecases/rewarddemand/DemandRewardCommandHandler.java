package com.onlydust.accounting.write.hexagon.usecases.rewarddemand;

import com.onlydust.accounting.write.hexagon.gateways.repositories.LedgerRepository;
import com.onlydust.accounting.write.hexagon.gateways.repositories.RewardDemandRepository;
import com.onlydust.accounting.write.hexagon.models.RewardDemand;
import com.onlydust.shared.write.hexagon.gateways.dateprovision.DateProvider;
import com.onlydust.shared.write.hexagon.gateways.repositories.DomainEventRepository;
import com.onlydust.shared.write.hexagon.gateways.uuidgeneration.UuidGenerator;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DemandRewardCommandHandler {

    private final RewardDemandRepository rewardDemandRepository;
    private final LedgerRepository ledgerRepository;
    private final DomainEventRepository domainEventRepository;
    private final UuidGenerator uuidGenerator;
    private final DateProvider dateProvider;

    public DemandRewardCommandHandler(RewardDemandRepository rewardDemandRepository,
                                      LedgerRepository ledgerRepository,
                                      DomainEventRepository domainEventRepository,
                                      UuidGenerator uuidGenerator,
                                      DateProvider dateProvider) {
        this.rewardDemandRepository = rewardDemandRepository;
        this.ledgerRepository = ledgerRepository;
        this.domainEventRepository = domainEventRepository;
        this.uuidGenerator = uuidGenerator;
        this.dateProvider = dateProvider;
    }

    public void handle(DemandRewardCommand demandRewardCommand) {
        ledgerRepository.byId(demandRewardCommand.ledgerId()).ifPresent(ledger -> {
            var remainingAmount = ledgerRepository.getRemainingAmount(ledger.getId());
            RewardDemand rewardDemand = ledger.demandReward(
                    uuidGenerator.generate(),
                    demandRewardCommand.amount(),
                    dateProvider.now(), remainingAmount);
            rewardDemandRepository.save(rewardDemand);
            ledger.getRegisteredDomainEvents().forEach(domainEventRepository::save);
        });
    }

}
