package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "currencies", schema = "public")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@TypeDefs({
        @TypeDef(name = "currency_type", typeClass = PostgreSQLEnumType.class),
        @TypeDef(name = "currency_standard", typeClass = PostgreSQLEnumType.class)
})
public class CurrencyEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID id;
    @org.hibernate.annotations.Type(type = "currency_type")
    @Enumerated(EnumType.STRING)
    private @NonNull Type type;
    @org.hibernate.annotations.Type(type = "currency_standard")
    @Enumerated(EnumType.STRING)
    private Standard standard;
    private @NonNull String name;
    private @NonNull String code;
    private @NonNull String logoUrl;
    private @NonNull Integer decimals;
    private String description;

    public static CurrencyEntity of(Currency currency) {
        return CurrencyEntity.builder()
                .id(currency.id().value())
                .type(Type.of(currency.type()))
                .standard(currency.standard().map(Standard::of).orElse(null))
                .name(currency.name())
                .code(currency.code().toString())
                .logoUrl(Objects.requireNonNull(currency.logoUri().map(Objects::toString).orElse(null)))
                .decimals(currency.decimals())
                .description(currency.description().orElse(null))
                .build();
    }

    public Currency toDomain() {
        return Currency.builder()
                .id(Currency.Id.of(id))
                .type(type.toDomain())
                .standard(standard == null ? null : standard.toDomain())
                .name(name)
                .code(Currency.Code.of(code))
                .metadata(new Currency.Metadata(name, description, URI.create(logoUrl)))
                .decimals(decimals)
                .build();
    }

    public enum Type {
        FIAT, CRYPTO;

        public static Type of(final @NonNull Currency.Type type) {
            return switch (type) {
                case FIAT -> FIAT;
                case CRYPTO -> CRYPTO;
            };
        }

        public Currency.Type toDomain() {
            return switch (this) {
                case FIAT -> Currency.Type.FIAT;
                case CRYPTO -> Currency.Type.CRYPTO;
            };
        }
    }

    public enum Standard {
        ISO4217, ERC20;

        public static Standard of(final @NonNull Currency.Standard standard) {
            return switch (standard) {
                case ISO4217 -> ISO4217;
                case ERC20 -> ERC20;
            };
        }

        public Currency.Standard toDomain() {
            return switch (this) {
                case ISO4217 -> Currency.Standard.ISO4217;
                case ERC20 -> Currency.Standard.ERC20;
            };
        }
    }
}
