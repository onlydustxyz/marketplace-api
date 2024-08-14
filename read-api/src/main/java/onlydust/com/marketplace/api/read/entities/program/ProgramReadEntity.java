package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ProgramPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ProgramResponse;
import onlydust.com.marketplace.api.contract.model.ProgramShortResponse;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;

import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper.map;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "sponsors", schema = "public") //  TODO change to programs after migration
@Immutable
@Accessors(fluent = true)
public class ProgramReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NonNull
    String name;

    @ManyToMany
    @JoinTable(
            name = "sponsors_users",
            joinColumns = @JoinColumn(name = "sponsor_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "userId")
    )
    @NonNull
    @OrderBy("login")
    List<AllUserReadEntity> leads;

    @OneToOne(optional = false, mappedBy = "program")
    @NonNull
    ProgramStatReadEntity stats;

    @OneToMany(mappedBy = "program")
    @NonNull
    List<ProgramStatPerCurrencyReadEntity> statsPerCurrency;

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
