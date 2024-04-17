package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
    private final UUID id;

    private final UUID currencyId;

    public static AccountBookEntity of(UUID currencyId) {
        return new AccountBookEntity(UUID.randomUUID(), currencyId);
    }

}
