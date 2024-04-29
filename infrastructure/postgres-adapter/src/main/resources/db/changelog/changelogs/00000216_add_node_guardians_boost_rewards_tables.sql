create table node_guardians_boost_rewards
(
    boosted_reward_id uuid      not null,
    boost_reward_id   uuid,
    recipient_id      bigint    not null,
    tech_created_at   TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at   TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (boosted_reward_id, recipient_id)
);

create table node_guardians_rewards_boost_outbox_events
(
    id         bigserial
        primary key,
    payload    jsonb                                                                    not null,
    status     public.outbox_event_status default 'PENDING'::public.outbox_event_status not null,
    error      text,
    created_at timestamp                  default now()                                 not null,
    updated_at timestamp                                                                not null,
    group_key  text
);