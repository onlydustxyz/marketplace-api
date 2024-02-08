package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;

@AllArgsConstructor
@Slf4j
public class CustomProjectTagRepository {

    private final EntityManager entityManager;

    private static final String TRUNCATE_PROJECTS_TAGS = "truncate table projects_tags";

    public void deleteAllTags() {
        final int executeUpdate = entityManager.createNativeQuery(TRUNCATE_PROJECTS_TAGS)
                .executeUpdate();
        LOGGER.info("{} project's tags were deleted", executeUpdate);
    }

    private static final String UPDATE_HOT_COMMUNITY = """
            insert into projects_tags (project_id, tag)
            select pts.project_id, 'HOT_COMMUNITY'
            from project_tag_scores pts
            where pts.hot_community_score >= 10""";

    public void updateHotCommunityTags() {
        final int executeUpdate = entityManager.createNativeQuery(UPDATE_HOT_COMMUNITY)
                .executeUpdate();
        LOGGER.info("{} projects were updated with tag HOT_COMMUNITY", executeUpdate);
    }

    private static final String UPDATE_NEWBIES_WELCOME = """
            insert into projects_tags (project_id, tag)
            select pts.project_id, 'NEWBIES_WELCOME'
            from project_tag_scores pts
            where pts.newbies_welcome_score > 5""";

    public void updateNewbiesWelcome() {
        final int executeUpdate = entityManager.createNativeQuery(UPDATE_NEWBIES_WELCOME)
                .executeUpdate();
        LOGGER.info("{} projects were updated with tag NEWBIES_WELCOME", executeUpdate);
    }

    private static final String UPDATE_LIKELY_TO_REWARD = """
            insert into projects_tags (project_id, tag)
            select pts.project_id, 'LIKELY_TO_REWARD'
            from project_tag_scores pts
            where pts.likely_to_reward_score > 3""";

    public void updateLikelyToReward() {
        final int executeUpdate = entityManager.createNativeQuery(UPDATE_LIKELY_TO_REWARD)
                .executeUpdate();
        LOGGER.info("{} projects were updated with tag LIKELY_TO_REWARD", executeUpdate);
    }

    private static final String UPDATE_WORK_IN_PROGRESS = """
            insert into projects_tags (project_id, tag)
            select pts.project_id, 'WORK_IN_PROGRESS'
            from project_tag_scores pts
            where pts.work_in_progress_score > 10""";

    public void updateWorkInProgress() {
        final int executeUpdate = entityManager.createNativeQuery(UPDATE_WORK_IN_PROGRESS)
                .executeUpdate();
        LOGGER.info("{} projects were updated with tag WORK_IN_PROGRESS", executeUpdate);
    }

    private static final String UPDATE_FAST_AND_FURIOUS = """
            insert into projects_tags (project_id, tag)
            select pts.project_id, 'FAST_AND_FURIOUS'
            from project_tag_scores pts
            where pts.fast_and_furious_score > 10""";

    public void updateFastAndFurious() {
        final int executeUpdate = entityManager.createNativeQuery(UPDATE_FAST_AND_FURIOUS)
                .executeUpdate();
        LOGGER.info("{} projects were updated with tag FAST_AND_FURIOUS", executeUpdate);
    }

    private static final String UPDATE_BIG_WHALE = """
            insert into projects_tags (project_id, tag)
            select pts.project_id, 'BIG_WHALE'
            from project_tag_scores pts
            where pts.big_whale_score > 10""";

    public void updateBigWhale() {
        final int executeUpdate = entityManager.createNativeQuery(UPDATE_BIG_WHALE)
                .executeUpdate();
        LOGGER.info("{} projects were updated with tag BIG_WHALE", executeUpdate);
    }
}
