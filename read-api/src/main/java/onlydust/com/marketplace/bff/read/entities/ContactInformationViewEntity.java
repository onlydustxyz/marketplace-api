package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.ContactInformation;
import onlydust.com.marketplace.api.contract.model.ContactInformationChannel;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "contact_informations", schema = "public")
@Value
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
    @NonNull Channel channel;

    @NonNull String contact;

    @Column(name = "public")
    @NonNull Boolean isPublic;

    public ContactInformation toDto() {
        return new ContactInformation()
                .channel(channel.toDto())
                .contact(contact)
                .visibility(isPublic ? ContactInformation.VisibilityEnum.PUBLIC : ContactInformation.VisibilityEnum.PRIVATE);
    }

    @EqualsAndHashCode
    static class PrimaryKey implements Serializable {
        UUID userId;
        @Enumerated(EnumType.STRING)
        @JdbcType(PostgreSQLEnumJdbcType.class)
        Channel channel;
    }

    public enum Channel {
        email, telegram, twitter, discord, linkedin, whatsapp;

        public ContactInformationChannel toDto() {
            return switch (this) {
                case email -> ContactInformationChannel.EMAIL;
                case telegram -> ContactInformationChannel.TELEGRAM;
                case twitter -> ContactInformationChannel.TWITTER;
                case discord -> ContactInformationChannel.DISCORD;
                case linkedin -> ContactInformationChannel.LINKEDIN;
                case whatsapp -> ContactInformationChannel.WHATSAPP;
            };
        }
    }
}
