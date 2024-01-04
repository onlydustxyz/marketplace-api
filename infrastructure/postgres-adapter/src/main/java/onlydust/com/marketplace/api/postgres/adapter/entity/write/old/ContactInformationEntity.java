package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ContactInformationIdEntity;

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
}
