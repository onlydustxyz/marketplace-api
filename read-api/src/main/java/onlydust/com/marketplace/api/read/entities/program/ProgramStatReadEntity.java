package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@Entity
@Table(name = "program_stats", schema = "bi")
public class ProgramStatReadEntity {
    @Id
    @NonNull
    UUID programId;

    @NonNull
    @OneToOne
    @JoinColumn(name = "programId", insertable = false, updatable = false)
    ProgramReadEntity program;

    int grantedProjectCount;

    int userCount;

    int rewardCount;
}
