package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.api.postgres.adapter.PostgresCurrencyAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresSponsorAccountStorageAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
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

    static final Currency currency = Currency.crypto("Ether", Currency.Code.of("ETH"), 18);

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
        final var ledger = new SponsorAccount(SponsorId.of(sponsor.getId()), currency);

        // When
        adapter.save(ledger);

        // Then
        final var savedLedger = adapter.get(ledger.id());
        assertThat(savedLedger).isPresent();
        assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
        assertThat(savedLedger.get().sponsorId()).isEqualTo(sponsorId);
        assertThat(savedLedger.get().currency()).isEqualTo(currency);
    }
}