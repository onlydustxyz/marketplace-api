package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ContactInformationIdEntity implements Serializable {
    @Column(name = "user_id", updatable = false, nullable = false)
    UUID userId;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "contact_channel", name = "channel", updatable = false, nullable = false)
    ContactChanelEnumEntity channel;
}
