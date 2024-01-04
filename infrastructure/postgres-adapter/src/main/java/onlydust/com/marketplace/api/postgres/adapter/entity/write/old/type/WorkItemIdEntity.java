package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemIdEntity implements Serializable {

  @Column(name = "payment_id")
  UUID paymentId;
  @Column(name = "repo_id")
  Integer repoId;
  @Column(name = "number")
  Integer number;

}
