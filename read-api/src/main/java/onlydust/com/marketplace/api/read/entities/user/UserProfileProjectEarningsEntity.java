package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.UserProfileProjectEarnings;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLinkViewEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class UserProfileProjectEarningsEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID projectId;

    @ManyToOne
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    @NonNull ProjectLinkViewEntity project;

    @NonNull BigDecimal totalEarnedUsd;

    public UserProfileProjectEarnings toDto() {
        return new UserProfileProjectEarnings()
                .projectName(project.name())
                .totalEarnedUsd(totalEarnedUsd)
                ;
    }
}
