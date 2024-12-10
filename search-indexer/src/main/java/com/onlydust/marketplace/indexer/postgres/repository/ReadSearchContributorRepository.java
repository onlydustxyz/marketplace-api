package com.onlydust.marketplace.indexer.postgres.repository;

import com.onlydust.marketplace.indexer.postgres.entity.SearchContributorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReadSearchContributorRepository extends JpaRepository<SearchContributorEntity, Long> {


    @Query(value = """
            select ga.id                                                github_id,
                   ga.login                                             github_login,
                   ga.bio,
                   ga.html_url,
                   coalesce((select array_length(contributed_on_project_ids, 1)
                             from bi.p_contributor_global_data pcgd
                             where pcgd.contributor_id = ga.id), 0)     project_count,
                   count(c.id) filter ( where c.type = 'PULL_REQUEST' ) pull_request_count,
                   count(c.id) filter ( where c.type = 'ISSUE' )        issue_count,
                   count(c.id)                                          contribution_count
            from indexer_exp.github_accounts ga
                     left join indexer_exp.contributions c on c.contributor_id = ga.id
            where ga.type = 'USER'
            group by ga.id, ga.login, ga.bio, ga.html_url
            """, nativeQuery = true)
    List<SearchContributorEntity> findAll();
}
