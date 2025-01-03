package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.ProgramLinkResponse;
import onlydust.com.backoffice.api.contract.model.ProgramWithBudgetResponse;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorStatPerCurrencyPerProgramReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Comparator.comparing;
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

    String url;
    String logoUrl;

    @ManyToOne
    @NonNull
    @JoinColumn(name = "sponsorId")
    SponsorReadEntity sponsor;

    @ManyToMany
    @JoinTable(
            name = "program_leads",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "userId")
    )
    @NonNull
    @OrderBy("login")
    List<AllUserReadEntity> leads;

    @OneToMany(mappedBy = "program")
    @NonNull
    List<ProgramStatReadEntity> stats;

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
    Set<ProjectReadEntity> grantedProjects;

    @OneToMany(mappedBy = "programId", fetch = FetchType.LAZY)
    @NonNull
    Set<ProgramStatPerCurrencyPerProjectReadEntity> perProjectStatsPerCurrency;

    @ManyToMany
    @NonNull
    @JoinTable(
            name = "sponsor_stats_per_currency_per_program",
            schema = "bi",
            joinColumns = @JoinColumn(name = "programId"),
            inverseJoinColumns = @JoinColumn(name = "sponsorId")
    )
    Set<SponsorReadEntity> allocatingSponsors;

    @OneToMany(mappedBy = "programId", fetch = FetchType.LAZY)
    @NonNull
    Set<SponsorStatPerCurrencyPerProgramReadEntity> perSponsorStatsPerCurrency;

    public ProgramStatReadEntity stats() {
        return stats.get(0);
    }

    public ProgramShortResponse toShortResponse() {
        return new ProgramShortResponse()
                .id(id)
                .name(name)
                .logoUrl(Optional.ofNullable(logoUrl).map(URI::create).orElse(null))
                .url(Optional.ofNullable(url).map(URI::create).orElse(null));
    }

    public ProgramResponse toResponse() {
        return new ProgramResponse()
                .id(id)
                .name(name)
                .url(Optional.ofNullable(url).map(URI::create).orElse(null))
                .logoUrl(Optional.ofNullable(logoUrl).map(URI::create).orElse(null))
                .leads(leads.stream().map(AllUserReadEntity::toRegisteredUserResponse).toList())
                .totalAvailable(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalAvailable))
                .totalGranted(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalGranted))
                .totalRewarded(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalRewarded))
                .projectCount(stats().grantedProjectCount())
                .contributorCount(stats().userCount())
                .rewardCount(stats().rewardCount());
    }

    public ProgramPageItemResponse toPageItemResponse() {
        return new ProgramPageItemResponse()
                .id(id)
                .name(name)
                .url(Optional.ofNullable(url).map(URI::create).orElse(null))
                .logoUrl(Optional.ofNullable(logoUrl).map(URI::create).orElse(null))
                .leads(leads.stream().map(AllUserReadEntity::toRegisteredUserResponse).toList())
                .projectCount(stats().grantedProjectCount())
                .totalAvailable(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalAvailable))
                .totalGranted(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalGranted))
                .totalRewarded(map(statsPerCurrency, ProgramStatPerCurrencyReadEntity::totalRewarded));
    }

    public ProgramLinkResponse toBoLinkResponse() {
        return new ProgramLinkResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl);
    }

    public onlydust.com.marketplace.api.contract.model.ProgramLinkResponse toLinkResponse() {
        return new onlydust.com.marketplace.api.contract.model.ProgramLinkResponse()
                .id(id)
                .name(name)
                .logoUrl(Optional.ofNullable(logoUrl).map(URI::create).orElse(null));
    }

    public ProgramWithBudgetResponse toBoProgramWithBudgetResponse() {
        return new ProgramWithBudgetResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .remainingBudgets(statsPerCurrency.stream()
                        .map(s -> new MoneyResponse()
                                .currency(s.currency().toBoShortResponse())
                                .amount(s.totalAvailable()))
                        .toList());
    }

    public SponsorProgramPageItemResponse toSponsorProgramPageItemResponse(UUID sponsorId) {
        final var statsPerCurrency = perSponsorStatsPerCurrency.stream()
                .filter(s -> s.sponsorId().equals(sponsorId))
                .toList();
        return new SponsorProgramPageItemResponse()
                .id(id)
                .name(name)
                .logoUrl(Optional.ofNullable(logoUrl).map(URI::create).orElse(null))
                .leads(leads.stream().map(AllUserReadEntity::toRegisteredUserResponse).toList())
                .projectCount(stats().grantedProjectCount())
                .userCount(stats().userCount())
                .totalAvailable(map(statsPerCurrency, SponsorStatPerCurrencyPerProgramReadEntity::totalAvailable))
                .totalGranted(map(statsPerCurrency, SponsorStatPerCurrencyPerProgramReadEntity::totalGranted))
                .totalAllocated(map(statsPerCurrency, SponsorStatPerCurrencyPerProgramReadEntity::totalAllocated));
    }

    public ProjectProgramPageItemResponse toProjectProgramPageItemResponse(final UUID projectId) {
        final var statsPerCurrency = perProjectStatsPerCurrency.stream()
                .filter(s -> s.projectId().equals(projectId))
                .toList();
        return new ProjectProgramPageItemResponse()
                .id(id)
                .name(name)
                .logoUrl(Optional.ofNullable(logoUrl).map(URI::create).orElse(null))
                .leads(leads.stream().map(AllUserReadEntity::toRegisteredUserResponse).toList())
                .totalAvailable(map(statsPerCurrency, ProgramStatPerCurrencyPerProjectReadEntity::totalAvailable))
                .totalGranted(map(statsPerCurrency, ProgramStatPerCurrencyPerProjectReadEntity::totalGranted))
                .totalRewarded(map(statsPerCurrency, ProgramStatPerCurrencyPerProjectReadEntity::totalRewarded));
    }

    public ProgramDetailsResponse toBoDetailsResponse() {
        return new ProgramDetailsResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .projects(grantedProjects.stream()
                        .map(p -> p.toBoProjectWithBudgetResponse(id))
                        .sorted(comparing(ProjectWithBudgetResponse::getName))
                        .toList());
    }
}
