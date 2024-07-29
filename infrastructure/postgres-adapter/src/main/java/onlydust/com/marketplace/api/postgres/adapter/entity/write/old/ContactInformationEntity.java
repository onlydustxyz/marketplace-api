package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ContactInformationIdEntity;
import onlydust.com.marketplace.project.domain.model.Contact;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "contact_informations", schema = "public")
public class ContactInformationEntity {

    @EmbeddedId
    ContactInformationIdEntity id;
    @Column(name = "contact", nullable = false)
    String contact;
    @Column(name = "public", nullable = false)
    Boolean isPublic;

    public Contact toDomain() {
        return Contact.builder()
                .contact(contact)
                .visibility(isPublic ? Contact.Visibility.PUBLIC : Contact.Visibility.PRIVATE)
                .channel(id.getChannel())
                .build();
    }
}
