package onlydust.com.marketplace.api.read.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.ShortCurrencyResponse;
import org.hibernate.annotations.Immutable;

import java.net.URI;
import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@Table(name = "currencies", schema = "public")
public class ShortCurrencyResponseEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID id;

    private @NonNull String code;
    private @NonNull String name;
    private String logoUrl;
    private @NonNull Integer decimals;

    public ShortCurrencyResponse toDto() {
        return new ShortCurrencyResponse()
                .id(id)
                .code(code)
                .name(name)
                .logoUrl(logoUrl == null ? null : URI.create(logoUrl))
                .decimals(decimals)
                ;
    }
}
