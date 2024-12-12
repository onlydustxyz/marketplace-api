package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonProjectIssuesReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface HackathonProjectIssuesReadRepository extends Repository<HackathonProjectIssuesReadEntity, UUID> {

    @Query(value = """
            SELECT p.project_id                        as id,
                   p.project_name                      as name,
                   p.project ->> 'logoUrl'             as logo_url,
                   p.project_slug                      as slug,
                   count(distinct i.contribution_uuid) as issue_count
            FROM hackathons h
                     JOIN hackathon_projects hp ON h.id = hp.hackathon_id
                     JOIN bi.p_project_global_data p ON p.project_id = hp.project_id AND
                                                        (coalesce(:languageIds) IS NULL OR p.language_ids && :languageIds)
                     JOIN indexer_exp.github_labels gl ON gl.name = ANY (h.github_labels)
                     JOIN bi.p_contribution_data i ON i.project_id = hp.project_id AND
                                                      i.contribution_type = 'ISSUE' AND
                                                      gl.id = any (i.github_label_ids) AND
                                                      (coalesce(:statuses) IS NULL OR i.contribution_status = ANY (cast(:statuses as indexer_exp.contribution_status[]))) AND
                                                      (coalesce(:search) IS NULL OR i.github_title ILIKE '%' || :search || '%')
                     JOIN indexer_exp.github_labels igl ON igl.id = ANY (i.github_label_ids) AND
                                                           (:isGoodFirstIssue IS NULL OR
                                                            :isGoodFirstIssue = TRUE AND igl.name ILIKE '%good%first%issue%' OR
                                                            :isGoodFirstIssue = FALSE AND igl.name NOT ILIKE '%good%first%issue%')
                     JOIN bi.p_contribution_contributors_data ccd ON ccd.contribution_uuid = i.contribution_uuid AND
                                                                     (:isAssigned IS NULL OR
                                                                      :isAssigned = TRUE AND coalesce(array_length(ccd.assignee_ids, 1), 0) > 0 OR
                                                                      :isAssigned = FALSE AND coalesce(array_length(ccd.assignee_ids, 1), 0) = 0) AND
                                                                     (:isApplied IS NULL OR
                                                                      :isApplied = TRUE AND coalesce(array_length(ccd.applicant_ids, 1), 0) > 0 OR
                                                                      :isApplied = FALSE AND coalesce(array_length(ccd.applicant_ids, 1), 0) = 0)
            WHERE h.id = :hackathonId
              AND (:isAvailable IS NULL
                OR :isAvailable = TRUE AND i.contribution_status = 'IN_PROGRESS' AND coalesce(array_length(ccd.assignee_ids, 1), 0) = 0
                OR :isAvailable = FALSE AND (i.contribution_status != 'IN_PROGRESS' OR coalesce(array_length(ccd.assignee_ids, 1), 0) > 0))
            GROUP BY p.project_id
            """, nativeQuery = true)
    List<HackathonProjectIssuesReadEntity> findAll(@NonNull UUID hackathonId,
                                                   String[] statuses,
                                                   Boolean isAssigned,
                                                   Boolean isApplied,
                                                   Boolean isAvailable,
                                                   Boolean isGoodFirstIssue,
                                                   UUID[] languageIds,
                                                   String search);
}
