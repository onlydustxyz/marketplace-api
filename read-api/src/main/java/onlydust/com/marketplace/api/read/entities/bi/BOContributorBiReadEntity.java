package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.BOBiContributorGlobalData;
import onlydust.com.backoffice.api.contract.model.BOBiContributorListItemResponse;
import onlydust.com.backoffice.api.contract.model.BOBiContributorProjectData;
import onlydust.com.backoffice.api.contract.model.ContributorOverviewResponse;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@NoArgsConstructor(force = true)
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
public class BOContributorBiReadEntity {
    @Id
    @NonNull
    Long contributorId;
    String contributorLogin;
    String telegram;
    Integer maintainedProjectCount;
    @JdbcTypeCode(SqlTypes.JSON)
    ContributorOverviewResponse contributor;
    @JdbcTypeCode(SqlTypes.JSON)
    BOBiContributorGlobalData globalData;
    @JdbcTypeCode(SqlTypes.JSON)
    List<BOBiContributorProjectData> perProjectData;

    public BOBiContributorListItemResponse toBoDto() {
        return new BOBiContributorListItemResponse()
                .id(contributorId)
                .login(contributorLogin)
                .telegram(telegram)
                .maintainedProjectCount(maintainedProjectCount)
                .contributor(contributor)
                .globalData(globalData)
                .perProjectData(perProjectData);
    }
}
