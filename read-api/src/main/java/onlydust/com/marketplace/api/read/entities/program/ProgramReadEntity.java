package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ProgramPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ProgramResponse;
import onlydust.com.marketplace.api.contract.model.ProgramShortResponse;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;

import java.util.List;
import java.util.UUID;

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

    @Formula("""
            (select count(distinct s.project_id)
             from accounting.program_stats_per_project s
             where s.program_id = id and s.total_granted > 0)
            """)
    Integer grantedProjectCount;

    public ProgramShortResponse toShortResponse() {
        return new ProgramShortResponse()
                .id(id)
                .name(name);
    }

    public ProgramResponse toResponse() {
        return new ProgramResponse()
                .id(id)
                .name(name);
    }

    public ProgramPageItemResponse toPageItemResponse() {
        return new ProgramPageItemResponse()
                .id(id)
                .name(name)
                .leads(leads.stream().map(AllUserReadEntity::toRegisteredUserResponse).toList())
                .projectCount(grantedProjectCount == null ? 0 : grantedProjectCount);
    }
}
