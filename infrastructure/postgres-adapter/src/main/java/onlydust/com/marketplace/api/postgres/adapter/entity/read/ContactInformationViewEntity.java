package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "contact_informations", schema = "public")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@IdClass(ContactInformationViewEntity.PrimaryKey.class)
public class ContactInformationViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "user_id")
    UUID userId;

    @Id
    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    @NonNull
    Channel channel;

    @NonNull
    String contact;

    @Column(name = "public")
    @NonNull
    Boolean isPublic;

    @EqualsAndHashCode
    static class PrimaryKey implements Serializable {
        UUID userId;
        @Enumerated(EnumType.STRING)
        @JdbcType(PostgreSQLEnumJdbcType.class)
        Channel channel;
    }

    public enum Channel {
        email, telegram, twitter, discord, linkedin, whatsapp;
    }
}
