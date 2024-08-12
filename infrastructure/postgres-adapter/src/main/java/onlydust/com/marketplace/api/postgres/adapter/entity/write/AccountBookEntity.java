package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;

import java.util.UUID;

@Entity
@Table(name = "account_books", schema = "accounting")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
public class AccountBookEntity {
    @Id
    @EqualsAndHashCode.Include
    private final @NonNull UUID id;

    private final @NonNull UUID currencyId;

    public static AccountBookEntity of(UUID currencyId) {
        return new AccountBookEntity(UUID.randomUUID(), currencyId);
    }

    public AccountBookAggregate toDomain() {
        return new AccountBookAggregate(AccountBookAggregate.Id.of(id));
    }
}
