package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@TypeDef(name = "contact_channel", typeClass = PostgreSQLEnumType.class)
public class ContactInformationIdEntity implements Serializable {

  @Column(name = "user_id", updatable = false, nullable = false)
  UUID userId;
  @Column(name = "channel", updatable = false, nullable = false)
  @Enumerated(EnumType.STRING)
  @Type(type = "contact_channel")
  ContactChanelEnumEntity channel;
}
