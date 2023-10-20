package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.AllocatedTimeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ContactChanelEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProfileCoverEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@TypeDef(name = "contact_channel", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "allocated_time", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "profile_cover", typeClass = PostgreSQLEnumType.class)
public class UserProfileEntity {
    @Id
    @Column(name = "row_number", nullable = false)
    Integer rowNumber;
    @Column(name = "github_user_id", nullable = false)
    Long githubId;
    @Column(name = "email")
    String email;
    @Column(name = "bio")
    String bio;
    @Column(name = "avatar_url", nullable = false)
    String avatarUrl;
    @Column(name = "login", nullable = false)
    String login;
    @Column(name = "html_url")
    String htmlUrl;
    @Column(name = "location")
    String location;
    @Column(name = "website")
    String website;
    @Column(name = "public")
    Boolean contactPublic;
    @Enumerated(EnumType.STRING)
    @Type(type = "contact_channel")
    @Column(name = "channel")
    ContactChanelEnumEntity contactChannel;
    @Column(name = "contact")
    String contact;
    @Column(name = "languages")
    String languages;
    @Enumerated(EnumType.STRING)
    @Type(type = "profile_cover")
    @Column(name = "cover")
    ProfileCoverEnumEntity cover;
    @Column(name = "last_seen")
    Date lastSeen;
    @Column(name = "created_at")
    Date createdAt;
    @Column(name = "code_review_count")
    Integer codeReviewCount;
    @Column(name = "issue_count")
    Integer issueCount;
    @Column(name = "pull_request_count")
    Integer pullRequestCount;
    @Column(name = "week")
    Integer week;
    @Column(name = "year")
    Integer year;
    @Column(name = "leading_project_number")
    Integer numberOfLeadingProject;
    @Column(name = "contributor_on_project")
    Integer numberOfOwnContributorOnProject;
    @Column(name = "contributions_count")
    Integer contributionsCount;
    @Column(name = "total_earned")
    BigDecimal totalEarned;
    @Column(name = "looking_for_a_job")
    Boolean isLookingForAJob;
    @Enumerated(EnumType.STRING)
    @Type(type = "allocated_time")
    @Column(name = "weekly_allocated_time")
    AllocatedTimeEnumEntity allocatedTimeToContribute;
    @Column(name = "id", nullable = false)
    private UUID id;

}
