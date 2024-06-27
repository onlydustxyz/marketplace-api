package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.api.read.entities.github.GithubIssueReadEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;


@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "projects_good_first_issues", schema = "public")
@Immutable
@IdClass(ProjectsGoodFirstIssuesReadEntity.PrimaryKey.class)
public class ProjectsGoodFirstIssuesReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @Column(nullable = false)
    UUID projectId;
    @Id
    @EqualsAndHashCode.Include
    @Column(nullable = false)
    Long issueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId")
    @NonNull
    ProjectReadEntity project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issueId")
    @NonNull
    GithubIssueReadEntity issue;

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        Long issueId;
    }
}
