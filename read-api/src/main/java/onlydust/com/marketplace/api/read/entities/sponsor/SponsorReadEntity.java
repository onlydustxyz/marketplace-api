package onlydust.com.marketplace.api.read.entities.sponsor;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.ProgramWithBudgetResponse;
import onlydust.com.backoffice.api.contract.model.SponsorDetailsResponse;
import onlydust.com.backoffice.api.contract.model.SponsorPageItemResponse;
import onlydust.com.marketplace.api.contract.model.SponsorLinkResponse;
import onlydust.com.marketplace.api.contract.model.SponsorResponse;
import onlydust.com.marketplace.api.read.entities.program.ProgramReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "sponsors", schema = "public")
@Immutable
@Accessors(fluent = true)
public class SponsorReadEntity {
    @Id
    @NonNull
    UUID id;

    @NonNull
    String name;

    @NonNull
    String url;

    String logoUrl;

    @OneToMany(mappedBy = "sponsorId", fetch = FetchType.LAZY)
    @NonNull
    Set<SponsorStatPerCurrencyPerProgramReadEntity> perProgramStatsPerCurrency;

    @OneToMany(mappedBy = "sponsorId", fetch = FetchType.LAZY)
    @NonNull
    Set<SponsorStatPerCurrencyReadEntity> statsPerCurrency;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sponsor_leads",
            schema = "public",
            joinColumns = @JoinColumn(name = "sponsorId"),
            inverseJoinColumns = @JoinColumn(name = "userId", referencedColumnName = "userId")
    )
    @NonNull
    Set<AllUserReadEntity> leads;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sponsors_programs",
            schema = "public",
            joinColumns = @JoinColumn(name = "sponsorId"),
            inverseJoinColumns = @JoinColumn(name = "programId")
    )
    @NonNull
    @OrderBy("name")
    List<ProgramReadEntity> programs;

    public SponsorResponse toResponse() {
        return new SponsorResponse()
                .id(id)
                .name(name)
                .url(url)
                .logoUrl(logoUrl);
    }

    public SponsorLinkResponse toLinkResponse() {
        return new SponsorLinkResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl);
    }

    public SponsorDetailsResponse toBoResponse() {
        return new SponsorDetailsResponse()
                .id(id)
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .availableBudgets(statsPerCurrency.stream()
                        .map(SponsorStatPerCurrencyReadEntity::toSponsorBudgetResponse)
                        .toList())
                .programs(perProgramStatsPerCurrency.stream()
                        .collect(groupingBy(SponsorStatPerCurrencyPerProgramReadEntity::program))
                        .entrySet().stream()
                        .map(e -> new ProgramWithBudgetResponse()
                                .id(e.getKey().id())
                                .name(e.getKey().name())
                                .logoUrl(e.getKey().logoUrl())
                                .remainingBudgets(e.getValue().stream()
                                        .map(stat -> stat.toBoMoneyResponse(s -> s.totalAllocated().subtract(s.totalGranted())))
                                        .toList()))
                        .toList())
                .leads(leads.stream()
                        .map(AllUserReadEntity::toBoLinkResponse)
                        .toList())
                ;
    }

    public SponsorPageItemResponse toBoPageItemResponse() {
        return new SponsorPageItemResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .url(url)
                .programs(programs.stream().map(ProgramReadEntity::toBoLinkResponse).toList());
    }
}
