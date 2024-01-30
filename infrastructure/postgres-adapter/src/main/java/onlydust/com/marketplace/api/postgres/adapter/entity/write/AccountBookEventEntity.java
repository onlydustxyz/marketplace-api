package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookEvent;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "account_books_events", schema = "sandbox")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Builder
public class AccountBookEventEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    private final UUID accountBookId;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", nullable = false)
    @NonNull
    private final Payload payload;

    public AccountBookEvent getEvent() {
        return payload.event;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Payload implements Serializable {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "className")
        private AccountBookEvent event;
    }
}
