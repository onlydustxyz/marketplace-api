-- Drop existing tables
DROP TABLE IF EXISTS reco.user_answers;
DROP TABLE IF EXISTS reco.matching_answers;
DROP TABLE IF EXISTS reco.matching_questions;

-- Create new user answers table with columns for each question
CREATE TABLE reco.user_answers_v1
(
    user_id               UUID PRIMARY KEY REFERENCES iam.users (id),
    primary_goals         UUID[],
    learning_preference   UUID,
    experience_level      UUID,
    languages             UUID[],
    ecosystems            UUID[],
    project_maturity      UUID,
    community_importance  UUID,
    long_term_involvement UUID,
    tech_created_at       timestamptz default now() not null,
    tech_updated_at       timestamptz default now() not null
);

create trigger reco_user_answers_v1_set_tech_updated_at
    before update
    on reco.user_answers_v1
    for each row
execute procedure public.set_tech_updated_at();

-- Create view for matching-v1 questions and answers
CREATE OR REPLACE VIEW reco.matching_questions_v1 AS
WITH languages_array AS (SELECT jsonb_agg(
                                        jsonb_build_object(
                                                'text', name,
                                                'id', md5(id::text)::uuid,
                                                'languageId', id
                                        ) ORDER BY name
                                ) as answers
                         FROM languages),
     ecosystems_array AS (SELECT jsonb_agg(
                                         jsonb_build_object(
                                                 'text', name,
                                                 'id', md5(id::text)::uuid,
                                                 'ecosystemId', id
                                         ) ORDER BY name
                                 ) || jsonb_build_array(
                                         jsonb_build_object(
                                                 'text', 'Don''t know',
                                                 'id', '00000000-0000-0000-0000-000000000000',
                                                 'ecosystemId', '00000000-0000-0000-0000-000000000000'
                                         )
                                      ) as answers
                          FROM ecosystems),
     static_answers AS (SELECT *
                        FROM (VALUES (0, jsonb_build_array(
                                jsonb_build_object('text', 'Learning new skills', 'id', 'c29f6545-5c2c-401d-9b88-925ac7836368'),
                                jsonb_build_object('text', 'Building a professional network', 'id', '1b23c5d4-8a7e-4f69-b6d2-7c45a9b8c012'),
                                jsonb_build_object('text', 'Gaining practical experience', 'id', '3d56e8f9-2b1a-4c7d-9e34-8f12b6d47890'),
                                jsonb_build_object('text', 'Supporting meaningful projects', 'id', 'a4b8c2d1-6e9f-4a5b-8c7d-2e3f4a5b6c7d'),
                                jsonb_build_object('text', 'Earning recognition in the community', 'id', 'f7e6d5c4-3b2a-1d9e-8f7c-6b5a4c3d2e1f')
                                         )),
                                     (1, jsonb_build_array(
                                             jsonb_build_object('text', 'Align with my skills', 'id', '9a8b7c6d-5e4f-3a2b-1c9d-8e7f6a5b4c3d', 'value', 1),
                                             jsonb_build_object('text', 'Challenge me to learn', 'id', 'b2c3d4e5-f6a7-8b9c-1d2e-3f4a5b6c7d8e', 'value', 2),
                                             jsonb_build_object('text', 'Both', 'id', 'd4e5f6a7-b8c9-1a2b-3c4d-5e6f7a8b9c0d', 'value', 0)
                                         )),
                                     (2, jsonb_build_array(
                                             jsonb_build_object('text', 'Beginner', 'id', 'e5f6a7b8-c9d0-1e2f-3a4b-5c6d7e8f9a0b', 'value', 1),
                                             jsonb_build_object('text', 'Intermediate', 'id', 'f6a7b8c9-d0e1-2f3a-4b5c-6d7e8f9a0b1c', 'value', 2),
                                             jsonb_build_object('text', 'Advanced', 'id', 'a7b8c9d0-e1f2-3a4b-5c6d-7e8f9a0b1c2d', 'value', 3),
                                             jsonb_build_object('text', 'Expert', 'id', 'b8c9d0e1-f2a3-4b5c-6d7e-8f9a0b1c2d3e', 'value', 4)
                                         )),
                                     (5, jsonb_build_array(
                                             jsonb_build_object('text', 'Well-established projects', 'id', 'c9d0e1f2-a3b4-5c6d-7e8f-9a0b1c2d3e4f', 'value', 1),
                                             jsonb_build_object('text', 'Emerging projects', 'id', 'd0e1f2a3-b4c5-6d7e-8f9a-0b1c2d3e4f5a', 'value', 2),
                                             jsonb_build_object('text', 'No preference', 'id', 'e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b', 'value', 0)
                                         )),
                                     (6, jsonb_build_array(
                                             jsonb_build_object('text', 'Very important', 'id', 'f2a3b4c5-d6e7-8f9a-0b1c-2d3e4f5a6b7c', 'value', 2),
                                             jsonb_build_object('text', 'Somewhat important', 'id', 'a3b4c5d6-e7f8-9a0b-1c2d-3e4f5a6b7c8d', 'value', 1),
                                             jsonb_build_object('text', 'Not important', 'id', 'b4c5d6e7-f8a9-0b1c-2d3e-4f5a6b7c8d9e', 'value', 0)
                                         )),
                                     (7, jsonb_build_array(
                                             jsonb_build_object('text', 'Very important', 'id', 'c5d6e7f8-a9b0-1c2d-3e4f-5a6b7c8d9e0f'),
                                             jsonb_build_object('text', 'Somewhat important', 'id', 'd6e7f8a9-b0c1-2d3e-4f5a-6b7c8d9e0f1a'),
                                             jsonb_build_object('text', 'Not important', 'id', 'e7f8a9b0-c1d2-3e4f-5a6b-7c8d9e0f1a2b')
                                         ))) AS a(q_index, answers)),
     questions AS (SELECT q_index as index,
                          question_id,
                          body,
                          description,
                          multiple_choice,
                          answers
                   FROM (SELECT q_index,
                                answers,
                                CASE q_index
                                    WHEN 0 THEN 'b98a375e-3a9d-4b63-a553-4d8d0c31d7c4'
                                    WHEN 1 THEN '2e44c33e-2c29-4f72-8d03-55aa1b83e3f1'
                                    WHEN 2 THEN '4f52195c-1c13-4c54-9132-a89e73e4c69d'
                                    WHEN 3 THEN '7d052a24-7824-43d8-8e7b-3727c2c1c9b4'
                                    WHEN 4 THEN 'e9e8e9b4-3c1a-4c54-9c3e-44c9b2c1c9b4'
                                    WHEN 5 THEN 'f1c2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d'
                                    WHEN 6 THEN 'a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d'
                                    WHEN 7 THEN 'b1c2d3e4-5f6a-7b8c-9d0e-1f2a3b4c5d6e'
                                    END as question_id,
                                CASE q_index
                                    WHEN 0 THEN 'What are your primary goals for contributing to open-source projects?'
                                    WHEN 1
                                        THEN 'Do you prefer contributing to projects that align with your current skills, challenge you to learn new ones, or both?'
                                    WHEN 2 THEN 'How would you rate your experience in software development?'
                                    WHEN 5 THEN 'Do you prefer working on well-established projects or emerging ones with room for innovation?'
                                    WHEN 6 THEN 'How important is an active community around an open-source project for you?'
                                    WHEN 7 THEN 'How important is long-term project involvement to you?'
                                    END as body,
                                CASE q_index
                                    WHEN 0 THEN 'Your goals help us understand what motivates you and find projects that align with your aspirations.'
                                    WHEN 1 THEN 'This helps us understand your learning preferences and comfort zone.'
                                    WHEN 2 THEN 'This helps us recommend projects matching your experience level.'
                                    WHEN 5 THEN 'Your preference helps us recommend projects at the right stage of development.'
                                    WHEN 6 THEN 'This helps us understand how much you value community interaction.'
                                    WHEN 7 THEN 'This helps us understand your preferred engagement duration.'
                                    END as description,
                                CASE q_index
                                    WHEN 0 THEN true
                                    ELSE false
                                    END as multiple_choice
                         FROM static_answers) q
                   UNION ALL
                   SELECT 3,
                          '7d052a24-7824-43d8-8e7b-3727c2c1c9b4',
                          'Which programming languages are you proficient in or interested in using?',
                          'Select the languages you''d like to work with in open source projects.',
                          true,
                          answers
                   FROM languages_array
                   UNION ALL
                   SELECT 4,
                          'e9e8e9b4-3c1a-4c54-9c3e-44c9b2c1c9b4',
                          'Which blockchain ecosystems are you interested in or curious about?',
                          'This helps us match you with projects in your preferred blockchain ecosystems.',
                          true,
                          answers
                   FROM ecosystems_array)
SELECT question_id                 AS id,
       index,
       body,
       description,
       multiple_choice,
       answers                     as possible_answers,
       jsonb_array_length(answers) as answers_count
FROM questions
ORDER BY index; 