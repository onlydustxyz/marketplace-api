package onlydust.com.marketplace.api.postgres.adapter.it;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.api.postgres.adapter.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectIdEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresLedgerStorageAdapterIT extends AbstractPostgresIT {
    @Autowired
    private PostgresLedgerStorageAdapter adapter;
    @Autowired
    private PostgresCurrencyAdapter postgresCurrencyAdapter;
    @Autowired
    private SponsorRepository sponsorRepository;
    @Autowired
    private ProjectIdRepository projectIdRepository;
    @Autowired
    private PostgresSponsorLedgerProviderAdapter sponsorLedgerProvider;
    @Autowired
    private PostgresContributorLedgerProviderAdapter contributorLedgerProvider;
    @Autowired
    private PostgresProjectLedgerProviderAdapter projectLedgerProvider;

    static final Currency currency = Currency.crypto("Ether", Currency.Code.of("ETH"), 18);
    final Faker faker = new Faker();
    final ContributorId contributorId = ContributorId.of(faker.number().randomNumber());

    @BeforeEach
    void setUp() {
        if (!postgresCurrencyAdapter.exists(currency.code()))
            postgresCurrencyAdapter.save(currency);
    }

    @Test
    void should_return_empty_when_ledger_not_found() {
        assertThat(adapter.get(Ledger.Id.random())).isEmpty();
    }

    @Test
    void should_return_sponsor_ledger_when_found() {
        // Given
        final SponsorEntity sponsor = new SponsorEntity(SponsorId.random().value(), "sponsor", "", "");
        sponsorRepository.save(sponsor);
        final var ledger = new Ledger(SponsorId.of(sponsor.getId()), currency);

        // When
        adapter.save(ledger);

        // Then
        {
            final var savedLedger = adapter.get(ledger.id());
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(sponsor.getId());
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }

        {
            final var savedLedger = sponsorLedgerProvider.get(SponsorId.of(sponsor.getId()), currency);
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(sponsor.getId());
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }
    }


    @Test
    void should_return_project_ledger_when_found() {
        // Given
        final ProjectIdEntity project = new ProjectIdEntity(ProjectId.random().value());
        projectIdRepository.save(project);
        final var ledger = new Ledger(ProjectId.of(project.getId()), currency);

        // When
        adapter.save(ledger);

        // Then
        {
            final var savedLedger = adapter.get(ledger.id());
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(project.getId());
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }

        {
            final var savedLedger = projectLedgerProvider.get(ProjectId.of(project.getId()), currency);
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(project.getId());
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }
    }


    @Test
    void should_return_contributor_ledger_when_found() {
        // Given
        final var ledger = new Ledger(contributorId, currency);

        // When
        adapter.save(ledger);

        // Then
        {
            final var savedLedger = adapter.get(ledger.id());
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(contributorId.value());
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }

        {
            final var savedLedger = contributorLedgerProvider.get(contributorId, currency);
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(contributorId.value());
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }
    }
}