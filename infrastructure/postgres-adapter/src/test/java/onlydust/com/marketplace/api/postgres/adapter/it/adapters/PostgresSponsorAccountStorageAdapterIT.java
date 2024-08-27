package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.api.postgres.adapter.PostgresCurrencyAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresSponsorAccountStorageAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import onlydust.com.marketplace.kernel.model.SponsorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresSponsorAccountStorageAdapterIT extends AbstractPostgresIT {
    @Autowired
    private PostgresSponsorAccountStorageAdapter adapter;
    @Autowired
    private PostgresCurrencyAdapter postgresCurrencyAdapter;
    @Autowired
    private SponsorRepository sponsorRepository;

    static final Currency currency = Currency.crypto("Ether", Currency.Code.of("ETH"), 18)
            .withMetadata(new Currency.Metadata(1027, "ETH", null, null));

    @BeforeEach
    void setUp() {
        if (!postgresCurrencyAdapter.exists(currency.code()))
            postgresCurrencyAdapter.save(currency);
    }

    @Test
    void should_return_empty_when_ledger_not_found() {
        assertThat(adapter.get(SponsorAccount.Id.random())).isEmpty();
    }

    @Test
    void should_return_sponsor_ledger_when_found() {
        // Given
        final var sponsorId = SponsorId.random();
        final SponsorEntity sponsor = new SponsorEntity(sponsorId.value(), "sponsor", "", "");
        sponsorRepository.save(sponsor);
        final var sponsorAccount = new SponsorAccount(SponsorId.of(sponsor.getId()), currency);

        // When
        adapter.save(sponsorAccount);

        // Then
        final var savedLedger = adapter.get(sponsorAccount.id());
        assertThat(savedLedger).isPresent();
        assertThat(savedLedger.get().id()).isEqualTo(sponsorAccount.id());
        assertThat(savedLedger.get().sponsorId()).isEqualTo(sponsorId);
        assertThat(savedLedger.get().currency()).isEqualTo(currency);
    }
}