package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookEvent;
import onlydust.com.marketplace.kernel.model.EventIdResolver;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_books_events", schema = "accounting")
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

    ZonedDateTime timestamp;

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
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
        @JsonTypeIdResolver(EventIdResolver.class)
        private AccountBookEvent event;
    }
}
