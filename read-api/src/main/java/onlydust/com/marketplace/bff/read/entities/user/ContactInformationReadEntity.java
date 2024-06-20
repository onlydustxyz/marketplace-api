package onlydust.com.marketplace.bff.read.entities.user;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.backoffice.api.contract.model.ContactInformation;
import onlydust.com.backoffice.api.contract.model.ContactInformationChannel;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.ContactChanelEnumEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "contact_informations", schema = "public")
@Value
@Immutable
public class ContactInformationReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "user_id")
    @NonNull
    UUID userId;

    @Id
    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    @NonNull
    ContactChanelEnumEntity channel;

    @NonNull String contact;

    @Column(name = "public")
    @NonNull
    Boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", insertable = false, updatable = false)
    AllUserReadEntity user;

    public ContactInformation toBODto() {
        return new ContactInformation()
                .channel(ContactInformationChannel.valueOf(channel.name().toUpperCase()))
                .contact(contact)
                .visibility(isPublic ? ContactInformation.VisibilityEnum.PUBLIC : ContactInformation.VisibilityEnum.PRIVATE);
    }

    public onlydust.com.marketplace.api.contract.model.ContactInformation toDto() {
        return new onlydust.com.marketplace.api.contract.model.ContactInformation()
                .channel(onlydust.com.marketplace.api.contract.model.ContactInformationChannel.valueOf(channel.name().toUpperCase()))
                .contact(contact)
                .visibility(isPublic ?
                        onlydust.com.marketplace.api.contract.model.ContactInformation.VisibilityEnum.PUBLIC :
                        onlydust.com.marketplace.api.contract.model.ContactInformation.VisibilityEnum.PRIVATE);
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        @NonNull UUID userId;
        @NonNull ContactChanelEnumEntity channel;
    }
}
