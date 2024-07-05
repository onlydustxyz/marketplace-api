package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationsToReviewByUser;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Immutable
public class ApplicationsToReviewQueryEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID userId;
    @NonNull
    String email;
    @NonNull
    String githubLogin;

    @NonNull
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectApplicationsToReviewByUser.Project> projects;

    public ProjectApplicationsToReviewByUser toDomain() {
        return new ProjectApplicationsToReviewByUser(userId, email, githubLogin, projects);
    }
}
