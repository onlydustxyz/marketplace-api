package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;

import javax.persistence.*;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Value
@Table(name = "project_ledgers", schema = "accounting")
public class ProjectLedgerEntity {
    @Id
    @NonNull UUID ledgerId;

    @OneToOne
    @JoinColumn(insertable = false, updatable = false, name = "ledger_id")
    @NonNull LedgerEntity ledger;

    @NonNull UUID projectId;

    public static ProjectLedgerEntity of(LedgerEntity ledger, ProjectId projectId) {
        return new ProjectLedgerEntity(ledger.id, ledger, projectId.value());
    }
}
