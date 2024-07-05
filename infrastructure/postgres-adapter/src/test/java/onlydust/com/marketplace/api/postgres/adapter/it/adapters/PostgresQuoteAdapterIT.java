package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.api.postgres.adapter.PostgresQuoteAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.OldestQuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.OldestQuoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresQuoteAdapterIT extends AbstractPostgresIT {

    @Autowired
    PostgresQuoteAdapter postgresQuoteAdapter;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    OldestQuoteRepository oldestQuoteRepository;

    @Test
    public void should_return_nearest_quote() {
        final var currencyId1 = Currency.Id.random();
        final var currencyId2 = Currency.Id.random();
        final var baseId1 = Currency.Id.random();
        final var baseId2 = Currency.Id.random();

        // Given
        currencyRepository.saveAllAndFlush(List.of(
                new CurrencyEntity(currencyId1.value(),
                        Currency.Type.CRYPTO,
                        "USD Coin", "USDC", "logo", 4, "foo", new String[]{}, null, Set.of()),
                new CurrencyEntity(currencyId2.value(),
                        Currency.Type.CRYPTO,
                        "Starknet", "STRK", "logo", 4, "foo", new String[]{}, null, Set.of()),
                new CurrencyEntity(baseId1.value(),
                        Currency.Type.FIAT,
                        "US Dollar", "USD", "logo", 4, "foo", new String[]{}, null, Set.of()),
                new CurrencyEntity(baseId2.value(),
                        Currency.Type.FIAT,
                        "Euro", "EUR", "logo", 4, "foo", new String[]{}, null, Set.of())
        ));
        postgresQuoteAdapter.save(List.of(
                new Quote(baseId1, currencyId1, BigDecimal.valueOf(1L), ZonedDateTime.of(2024, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC).toInstant()),
                new Quote(baseId1, currencyId2, BigDecimal.valueOf(2L), ZonedDateTime.of(2024, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC).toInstant()),

                new Quote(baseId1, currencyId1, BigDecimal.valueOf(3L), ZonedDateTime.of(2024, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC).toInstant()),
                new Quote(baseId1, currencyId2, BigDecimal.valueOf(4L), ZonedDateTime.of(2024, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC).toInstant()),

                new Quote(baseId1, currencyId1, BigDecimal.valueOf(5L), ZonedDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC).toInstant()),
                new Quote(baseId1, currencyId2, BigDecimal.valueOf(6L), ZonedDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC).toInstant()),

                new Quote(baseId1, currencyId1, BigDecimal.valueOf(7L), ZonedDateTime.of(2024, 1, 1, 9, 30, 0, 0, ZoneOffset.UTC).toInstant()),
                new Quote(baseId1, currencyId2, BigDecimal.valueOf(8L), ZonedDateTime.of(2024, 1, 1, 9, 30, 0, 0, ZoneOffset.UTC).toInstant()),

                new Quote(baseId2, currencyId1, BigDecimal.valueOf(1L), ZonedDateTime.of(2024, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC).toInstant()),
                new Quote(baseId2, currencyId2, BigDecimal.valueOf(2L), ZonedDateTime.of(2024, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC).toInstant()),

                new Quote(baseId2, currencyId1, BigDecimal.valueOf(3L), ZonedDateTime.of(2024, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC).toInstant()),
                new Quote(baseId2, currencyId2, BigDecimal.valueOf(4L), ZonedDateTime.of(2024, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC).toInstant()),

                new Quote(baseId2, currencyId1, BigDecimal.valueOf(5L), ZonedDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC).toInstant()),
                new Quote(baseId2, currencyId2, BigDecimal.valueOf(6L), ZonedDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC).toInstant()),

                new Quote(baseId2, currencyId1, BigDecimal.valueOf(7L), ZonedDateTime.of(2024, 1, 1, 9, 30, 0, 0, ZoneOffset.UTC).toInstant()),
                new Quote(baseId2, currencyId2, BigDecimal.valueOf(8L), ZonedDateTime.of(2024, 1, 1, 9, 30, 0, 0, ZoneOffset.UTC).toInstant())
        ));

        {
            // When
            var oldest = oldestQuoteRepository.findById(new OldestQuoteEntity.PrimaryKey(baseId1.value(), currencyId1.value())).orElseThrow();
            // Then
            assertThat(oldest.timestamp()).isEqualTo(ZonedDateTime.of(2024, 1, 1, 9, 30, 0, 0, ZoneOffset.UTC).toInstant());
            assertThat(oldest.price()).isEqualTo(BigDecimal.valueOf(7L));
        }
        {
            // When
            var latest = postgresQuoteAdapter.latest(baseId1, currencyId1).orElseThrow();
            // Then
            assertThat(latest.timestamp()).isEqualTo(ZonedDateTime.of(2024, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC).toInstant());
            assertThat(latest.price()).isEqualTo(BigDecimal.valueOf(1L));
        }
        {
            // When
            var nearest = postgresQuoteAdapter.nearest(baseId1, currencyId1, ZonedDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC)).orElseThrow();
            // Then
            assertThat(nearest.price()).isEqualTo(BigDecimal.valueOf(5L));
        }
        {
            // When
            var nearest = postgresQuoteAdapter.nearest(baseId1, currencyId1, ZonedDateTime.of(2024, 1, 1, 10, 3, 0, 0, ZoneOffset.UTC)).orElseThrow();
            // Then
            assertThat(nearest.price()).isEqualTo(BigDecimal.valueOf(5L));
        }
        {
            // When
            var nearest = postgresQuoteAdapter.nearest(baseId1, currencyId1, ZonedDateTime.of(2024, 1, 1, 9, 57, 0, 0, ZoneOffset.UTC)).orElseThrow();
            // Then
            assertThat(nearest.price()).isEqualTo(BigDecimal.valueOf(7L));
        }
        {
            // When
            var nearest = postgresQuoteAdapter.nearest(baseId1, currencyId1, ZonedDateTime.of(2024, 1, 1, 10, 15, 0, 0, ZoneOffset.UTC)).orElseThrow();
            // Then
            assertThat(nearest.price()).isEqualTo(BigDecimal.valueOf(5L));
        }
        {
            // When
            var nearest = postgresQuoteAdapter.nearest(baseId1, currencyId1, ZonedDateTime.of(2024, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC));
            // Then
            assertThat(nearest).isNotPresent();
        }
        {
            // When
            var nearest = postgresQuoteAdapter.nearest(baseId1, currencyId1, ZonedDateTime.of(2024, 1, 5, 0, 0, 0, 0, ZoneOffset.UTC)).orElseThrow();
            // Then
            assertThat(nearest.price()).isEqualTo(BigDecimal.valueOf(1L));
        }
    }
}
