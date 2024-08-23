package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ProgramPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ProgramResponse;
import onlydust.com.marketplace.api.contract.model.ProgramShortResponse;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;

import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper.map;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "programs", schema = "public")
@Immutable
@Accessors(fluent = true)
public class ProgramReadEntity {
    @Id
    @NonNull
    UUID id;

    @NonNull
    String name;

    String logoUrl;

    @ManyToMany
    @JoinTable(
            name = "program_leads",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "userId")
    )
    @NonNull
    @OrderBy("login")
    List<AllUserReadEntity> leads;

    @OneToOne(optional = false, mappedBy = "program")
    @NonNull
    ProgramStatReadEntity stats;

    @OneToMany(mappedBy = "programId")
    @NonNull
    List<ProgramStatPerCurrencyReadEntity> statsPerCurrency;

    @ManyToMany
    @NonNull
    @JoinTable(
            name = "program_stats_per_currency_per_project",
            schema = "bi",
            joinColumns = @JoinColumn(name = "programId"),
            inverseJoinColumns = @JoinColumn(name = "projectId")
    )
    List<ProjectReadEntity> grantedProjects;

    public ProgramShortResponse toShortResponse() {
        return new ProgramShortResponse()
                .id(id)
                .name(name);
    }

    public ProgramResponse toResponse() {
        return new ProgramResponse()
                .id(id)
                .name(name)
                .totalAvailable(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalAvailable))
                .totalGranted(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalGranted))
                .totalRewarded(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalRewarded));
    }

    public ProgramPageItemResponse toPageItemResponse() {
        return new ProgramPageItemResponse()
                .id(id)
                .name(name)
                .leads(leads.stream().map(AllUserReadEntity::toRegisteredUserResponse).toList())
                .projectCount(stats.grantedProjectCount())
                .totalAvailable(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalAvailable))
                .totalGranted(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalGranted))
                .totalRewarded(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalRewarded));
    }

}
