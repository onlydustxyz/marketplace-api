package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorAccountRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;

@AllArgsConstructor
public class PostgresSponsorAccountStorageAdapter implements SponsorAccountStorage {
    private final SponsorAccountRepository sponsorAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<SponsorAccount> get(SponsorAccount.Id id) {
        return sponsorAccountRepository.findById(id.value()).map(SponsorAccountEntity::toDomain);
    }

    @Override
    public void save(SponsorAccount... sponsorAccounts) {
        sponsorAccountRepository.saveAllAndFlush(Arrays.stream(sponsorAccounts).map(SponsorAccountEntity::of).toList());
    }

    @Override
    @Transactional
    public void delete(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction.Id transactionId) {
        final var sponsorAccount = sponsorAccountRepository.findById(sponsorAccountId.value())
                .orElseThrow(() -> OnlyDustException.notFound("Sponsor account %s not found".formatted(sponsorAccountId)));

        sponsorAccount.getTransactions().removeIf(t -> t.getId().equals(transactionId.value()));
        sponsorAccountRepository.saveAndFlush(sponsorAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SponsorAccount> getSponsorAccounts(SponsorId sponsorId) {
        return sponsorAccountRepository.findAllBySponsorId(sponsorId.value()).stream()
                .map(SponsorAccountEntity::toDomain)
                .sorted(comparing(a -> a.currency().code().toString().toUpperCase()))
                .toList();
    }

    @Override
    public List<SponsorAccount> find(SponsorId sponsorId, Currency.Id currencyId) {
        return sponsorAccountRepository.findBySponsorIdAndCurrencyId(sponsorId.value(), currencyId.value())
                .stream().map(SponsorAccountEntity::toDomain).toList();
    }
}
