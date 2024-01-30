package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Value
@Table(name = "project_ledgers", schema = "sandbox")
@IdClass(ProjectLedgerEntity.PrimaryKey.class)
public class ProjectLedgerEntity {
    @Id
    @NonNull UUID ledgerId;

    @Id
    @NonNull UUID projectId;

    public static ProjectLedgerEntity of(Ledger.Id ledgerId, ProjectId projectId) {
        return new ProjectLedgerEntity(ledgerId.value(), projectId.value());
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        private UUID ledgerId;
        private UUID projectId;
    }
}
