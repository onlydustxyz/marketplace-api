package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.kernel.model.EventIdResolver;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_books_events", schema = "accounting")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Accessors(fluent = true)
@IdClass(AccountBookEventEntity.PrimaryKey.class)
public class AccountBookEventEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    private final Long id;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    private final UUID accountBookId;

    ZonedDateTime timestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @NonNull
    private final Payload payload;

    public AccountBookEvent getEvent() {
        return payload.event;
    }

    @SuppressWarnings("unused") // it is used in the repository's native insert query
    public String payloadAsJsonString() throws JsonProcessingException {
        final var mapper = new ObjectMapper();
        return mapper.writeValueAsString(payload);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Payload {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
        @JsonTypeIdResolver(EventIdResolver.class)
        private AccountBookEvent event;
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        Long id;
        UUID accountBookId;
    }

    public static AccountBookEventEntity of(UUID accountBookId, IdentifiedAccountBookEvent<?> event) {
        return new AccountBookEventEntity(event.id(), accountBookId, event.timestamp(), new Payload(event.data()));
    }

    public IdentifiedAccountBookEvent toIdentifiedAccountBookEvent() {
        return new IdentifiedAccountBookEvent(id, timestamp, payload.event);
    }
}
