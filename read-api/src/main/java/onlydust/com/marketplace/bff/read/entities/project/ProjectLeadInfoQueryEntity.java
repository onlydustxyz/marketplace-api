package onlydust.com.marketplace.bff.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "projects", schema = "public")
@Value
@NoArgsConstructor(force = true)
public class ProjectLeadInfoQueryEntity {
    @Id
    UUID id;

    @Formula("""
            (select round(sum(laq.price * pa.current_allowance), 2)
            from project_allowances pa
                     join accounting.latest_usd_quotes laq on laq.currency_id = pa.currency_id
            where pa.project_id = id)
            """)
    BigDecimal remainingUsdBudget;

    @Formula("""
            (select count(pgr.github_repo_id) > count(agr.repo_id)
             from project_github_repos pgr
                      join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id and gr.visibility = 'PUBLIC'
                      left join indexer_exp.authorized_github_repos agr on agr.repo_id = pgr.github_repo_id
             where pgr.project_id = id)
            """)
    Boolean isMissingGithubAppInstallation;
}
