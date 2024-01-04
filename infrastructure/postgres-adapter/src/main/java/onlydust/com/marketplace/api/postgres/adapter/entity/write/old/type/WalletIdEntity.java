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
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
public class WalletIdEntity implements Serializable {

  @Column(name = "user_id")
  UUID userId;
  @Column(name = "network")
  @Enumerated(EnumType.STRING)
  @Type(type = "network")
  NetworkEnumEntity network;
}
