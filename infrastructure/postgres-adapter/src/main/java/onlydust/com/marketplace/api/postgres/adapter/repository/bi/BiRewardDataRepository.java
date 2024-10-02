package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class BiRewardDataRepository {
    private final @NonNull EntityManager entityManager;

    public int refresh() {
        final var query = entityManager.createNativeQuery("""
                delete from bi.p_reward_data
                where not exists(select 1 from bi.m_reward_data m
                                 where m.reward_id = p_reward_data.reward_id and
                                       m.hash = p_reward_data.hash);
                
                insert into bi.p_reward_data
                select * from bi.m_reward_data
                on conflict (reward_id) do nothing;
                """);
        return query.executeUpdate();
    }
}
