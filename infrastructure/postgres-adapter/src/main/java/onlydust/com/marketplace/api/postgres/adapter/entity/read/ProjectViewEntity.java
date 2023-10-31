package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@TypeDef(name = "project_visibility", typeClass = PostgreSQLEnumType.class)
public class ProjectViewEntity {

    @Id
    @Column(name = "row_number", nullable = false)
    Integer rowNumber;
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
    @Column(name = "rank", insertable = false)
    Integer rank;
    @Enumerated(EnumType.STRING)
    @Type(type = "project_visibility")
    @Column(columnDefinition = "visibility")
    ProjectVisibilityEnumEntity visibility;
    @Column(name = "p_lead_id", insertable = false)
    UUID projectLeadId;
    @Column(name = "p_lead_github_user_id", insertable = false)
    Long projectLeadGithubUserId;
    @Column(name = "p_lead_login", insertable = false)
    String projectLeadLogin;
    @Column(name = "p_lead_avatar_url", insertable = false)
    String projectLeadAvatarUrl;
    @Column(name = "p_lead_url", insertable = false)
    String projectLeadUrl;
    @Column(name = "sponsor_name", insertable = false)
    String sponsorName;
    @Column(name = "sponsor_logo_url", insertable = false)
    String sponsorLogoUrl;
    @Column(name = "sponsor_url", insertable = false)
    String sponsorUrl;
    @Column(name = "sponsor_id", insertable = false)
    UUID sponsorId;
    @Column(name = "repo_count", insertable = false)
    Integer repoCount;
    @Column(name = "contributors_count", insertable = false)
    Integer contributorsCount;
    @Column(name = "languages", insertable = false)
    String repositoryLanguages;
    @Column(name = "repository_id", insertable = false)
    Long repositoryId;
    @Column(name = "project_id", nullable = false)
    UUID id;
    @Column(name = "is_pending_project_lead", nullable = false)
    Boolean isPendingProjectLead;
}
