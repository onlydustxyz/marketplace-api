package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ContactInformation;
import onlydust.com.marketplace.api.contract.model.ContactInformationChannel;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "contact_informations", schema = "public")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
    ContactInformationChannel channel;

    @NonNull
    String contact;

    @Column(name = "public")
    @NonNull
    Boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", insertable = false, updatable = false)
    AllUserReadEntity user;

    public onlydust.com.backoffice.api.contract.model.ContactInformation toBODto() {
        return new onlydust.com.backoffice.api.contract.model.ContactInformation()
                .channel(onlydust.com.backoffice.api.contract.model.ContactInformationChannel.valueOf(channel.name().toUpperCase()))
                .contact(contact)
                .visibility(isPublic ?
                        onlydust.com.backoffice.api.contract.model.ContactInformation.VisibilityEnum.PUBLIC :
                        onlydust.com.backoffice.api.contract.model.ContactInformation.VisibilityEnum.PRIVATE);
    }

    public ContactInformation toDto() {
        return new ContactInformation()
                .channel(channel)
                .contact(contact)
                .visibility(isPublic ?
                        ContactInformation.VisibilityEnum.PUBLIC :
                        ContactInformation.VisibilityEnum.PRIVATE);
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        @NonNull
        UUID userId;
        @NonNull
        ContactInformationChannel channel;
    }
}
