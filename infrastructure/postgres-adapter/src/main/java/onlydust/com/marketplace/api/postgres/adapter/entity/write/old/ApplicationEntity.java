package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "applications", schema = "public")
public class ApplicationEntity {

    @Id
    @Column(name = "id")
    UUID id;
    @Column(name = "received_at", nullable = false)
    Date receivedAt;
    @Column(name = "project_id", nullable = false)
    UUID projectId;
    @Column(name = "applicant_id", nullable = false)
    UUID applicantId;
}
