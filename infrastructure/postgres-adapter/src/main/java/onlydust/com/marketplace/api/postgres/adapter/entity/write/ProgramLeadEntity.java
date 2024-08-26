package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Accessors(fluent = true)
@Table(name = "program_leads", schema = "public")
@IdClass(ProgramLeadEntity.PrimaryKey.class)
public class ProgramLeadEntity {
    @Id
    UUID userId;

    @Id
    UUID programId;

    public static ProgramLeadEntity of(ProgramId programId, UserId leadId) {
        return new ProgramLeadEntity(leadId.value(), programId.value());
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID programId;
    }
}
