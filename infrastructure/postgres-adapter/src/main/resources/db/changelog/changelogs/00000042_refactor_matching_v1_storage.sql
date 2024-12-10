-- Drop existing tables
DROP TABLE IF EXISTS reco.user_answers;
DROP TABLE IF EXISTS reco.matching_answers;
DROP TABLE IF EXISTS reco.matching_questions;

-- Create new user answers table with columns for each question
CREATE TABLE reco.user_answers_v1
(
    user_id               UUID PRIMARY KEY REFERENCES iam.users (id),
    primary_goals         int[],
    learning_preference   int,
    experience_level      int,
    languages             UUID[],
    ecosystems            UUID[],
    project_maturity      int,
    community_importance  int,
    long_term_involvement int,
    tech_created_at       timestamptz default now() not null,
    tech_updated_at       timestamptz default now() not null
);

create trigger reco_user_answers_v1_set_tech_updated_at
    before update
    on reco.user_answers_v1
    for each row
execute procedure public.set_tech_updated_at();
