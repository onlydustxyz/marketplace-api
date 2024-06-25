package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;


@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "project_members", schema = "public")
@IdClass(ProjectMemberReadEntity.PrimaryKey.class)
@Immutable
public class ProjectMemberReadEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID projectId;

    @Id
    @EqualsAndHashCode.Include
    Long githubUserId;

    UUID userId;

    @EqualsAndHashCode
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        Long githubUserId;
    }

}
