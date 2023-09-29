package onlydust.com.marketplace.api.postgres.adapter.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.ProjectEntity;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
public class ProjectViewEntity {

    @Id
    @Column(name = "row_number", nullable = false)
    Integer rowNumber;
    @Column(name = "project_id", nullable = false)
    private UUID id;
    @Column(name = "name")
    String name;
    @Column(name = "short_description")
    String shortDescription;
    @Column(name = "logo_url")
    String logoUrl;
    @Column(name = "hiring")
    Boolean hiring;
    @Column(name = "key", insertable = false)
    String key;
    @Enumerated(EnumType.STRING)
    @Type(type = "project_visibility")
    @Column(columnDefinition = "visibility")
    ProjectEntity.Visibility visibility;
    @Column(name = "p_lead_id", insertable = false)
    UUID projectLeadId;
    @Column(name = "p_lead_login", insertable = false)
    String projectLeadLogin;
    @Column(name = "p_lead_avatar_url", insertable = false)
    String projectLeadAvatarUrl;
    @Column(name = "sponsor_name", insertable = false)
    String sponsorName;
    @Column(name = "sponsor_logo_url", insertable = false)
    String sponsorLogoUrl;
    @Column(name = "sponsor_id", insertable = false)
    UUID sponsorId;
    @Column(name = "repo_count", insertable = false)
    Integer repoCount;
    @Column(name = "contributors_count", insertable = false)
    Integer contributorsCount;
    @Column(name = "languages", insertable = false)
    String repositoryLanguages;
    @Column(name = "repository_id", insertable = false)
    Integer repositoryId;



}
