package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Contact;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "contact_informations", schema = "public")
@IdClass(ContactInformationEntity.PrimaryKey.class)
public class ContactInformationEntity {

    @Id
    @EqualsAndHashCode.Include
    UUID userId;
    @Id
    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "contact_channel", nullable = false)
    Contact.Channel channel;
    String contact;
    @Column(name = "public", nullable = false)
    Boolean isPublic;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "userId", insertable = false)
    UserProfileInfoEntity userProfileInfo;

    public Contact toDomain() {
        return Contact.builder()
                .contact(contact)
                .visibility(isPublic ? Contact.Visibility.PUBLIC : Contact.Visibility.PRIVATE)
                .channel(channel)
                .build();
    }

    public static ContactInformationEntity fromDomain(UserId userId, Contact contact) {
        return ContactInformationEntity.builder()
                .userId(userId.value())
                .channel(contact.getChannel())
                .contact(contact.getContact())
                .isPublic(contact.getVisibility() == Contact.Visibility.PUBLIC)
                .build();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        Contact.Channel channel;
        UUID userId;
    }
}
