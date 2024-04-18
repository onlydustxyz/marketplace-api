package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

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