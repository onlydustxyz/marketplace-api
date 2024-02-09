ALTER TABLE accounting.reward_status_data
    RENAME CONSTRAINT reward_statuses_reward_id_fkey TO reward_status_data_reward_id_fkey;

ALTER TABLE accounting.reward_status_data
    ALTER CONSTRAINT reward_status_data_reward_id_fkey DEFERRABLE INITIALLY DEFERRED;
