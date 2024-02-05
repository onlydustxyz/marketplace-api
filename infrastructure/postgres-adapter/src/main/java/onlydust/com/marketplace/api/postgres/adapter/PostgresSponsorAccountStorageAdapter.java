package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorAccountRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PostgresSponsorAccountStorageAdapter implements SponsorAccountStorage {
    private final SponsorAccountRepository sponsorAccountRepository;

    @Override
    public Optional<SponsorAccount> get(SponsorAccount.Id id) {
        return sponsorAccountRepository.findById(id.value()).map(SponsorAccountEntity::toLedger);
    }

    @Override
    public void save(SponsorAccount... sponsorAccounts) {
        sponsorAccountRepository.saveAll(Arrays.stream(sponsorAccounts).map(SponsorAccountEntity::of).toList());
    }

    @Override
    public void deleteTransaction(SponsorAccount.Id sponsorAccountId, String reference) {
        final var sponsorAccount = sponsorAccountRepository.findById(sponsorAccountId.value())
                .orElseThrow(() -> OnlyDustException.notFound("Sponsor account %s not found".formatted(sponsorAccountId)));

        sponsorAccount.getTransactions().removeIf(t -> t.getReference().equals(reference));
        sponsorAccountRepository.save(sponsorAccount);
    }

    @Override
    public List<SponsorAccount> getSponsorAccounts(SponsorId sponsorId) {
        return sponsorAccountRepository.findAllBySponsorId(sponsorId.value()).stream().map(SponsorAccountEntity::toLedger).toList();
    }
}
