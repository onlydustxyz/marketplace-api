package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "hidden_contributors", schema = "public")
@IdClass(HiddenContributorEntity.PrimaryKey.class)
public class HiddenContributorEntity {
    @Id
    UUID projectId;
    @Id
    UUID projectLeadId;
    @Id
    Long contributorGithubUserId;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID projectLeadId;
        Long contributorGithubUserId;
    }
}