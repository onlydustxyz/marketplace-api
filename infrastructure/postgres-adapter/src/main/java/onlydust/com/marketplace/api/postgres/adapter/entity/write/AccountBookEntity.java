package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookEvent;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "accountBookId")
    @OrderBy("timestamp, id")
    private final List<AccountBookEventEntity> events = new ArrayList<>();

    public static AccountBookEntity of(UUID currencyId) {
        return new AccountBookEntity(UUID.randomUUID(), currencyId);
    }

    public void add(AccountBookEvent event) {
        events.add(AccountBookEventEntity.builder()
                .accountBookId(id)
                .timestamp(ZonedDateTime.now())
                .payload(new AccountBookEventEntity.Payload(event))
                .build());
    }
}
