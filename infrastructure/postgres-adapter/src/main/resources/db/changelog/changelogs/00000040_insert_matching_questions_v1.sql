INSERT INTO reco.matching_questions (id, matching_system_id, index, body, description, multiple_choice)
VALUES (gen_random_uuid(), 'matching-v1', 0, 'What are your primary goals for contributing to open-source projects?',
        'Your goals help us understand what motivates you and find projects that align with your aspirations.', true),
       (gen_random_uuid(), 'matching-v1', 1,
        'Do you prefer contributing to projects that align with your current skills, challenge you to learn new ones, or both?',
        'This helps us understand your learning preferences and comfort zone.', false),
       (gen_random_uuid(), 'matching-v1', 2, 'How would you rate your experience in software development?',
        'This helps us recommend projects matching your experience level.', false),
       (gen_random_uuid(), 'matching-v1', 3, 'Which programming languages are you proficient in or interested in using?',
        'Select the languages you''d like to work with in open source projects.', true),
       (gen_random_uuid(), 'matching-v1', 4, 'Which blockchain ecosystems are you interested in or curious about?',
        'This helps us match you with projects in your preferred blockchain ecosystems.', true),
       (gen_random_uuid(), 'matching-v1', 5, 'Do you prefer working on well-established projects or emerging ones with room for innovation?',
        'Your preference helps us recommend projects at the right stage of development.', false),
       (gen_random_uuid(), 'matching-v1', 6, 'How important is an active community around an open-source project for you?',
        'This helps us understand how much you value community interaction.', false),
       (gen_random_uuid(), 'matching-v1', 7, 'How important is long-term project involvement to you?',
        'This helps us understand your preferred engagement duration.', false);

WITH questions AS (SELECT id, index
                   FROM reco.matching_questions
                   WHERE matching_system_id = 'matching-v1'
                   ORDER BY index)
INSERT
INTO reco.matching_answers (id, question_id, index, body)
SELECT gen_random_uuid(),
       q.id,
       a.index,
       a.body
FROM questions q
         CROSS JOIN LATERAL (
    VALUES
        -- Question 0: Primary goals
        (0, 0, 'Learning new skills'),
        (0, 1, 'Building a professional network'),
        (0, 2, 'Gaining practical experience'),
        (0, 3, 'Supporting meaningful projects'),
        (0, 4, 'Earning recognition in the community'),
        -- Question 1: Skills preference
        (1, 0, 'Align with my skills'),
        (1, 1, 'Challenge me to learn'),
        (1, 2, 'Both'),
        -- Question 2: Experience level
        (2, 0, 'Beginner'),
        (2, 1, 'Intermediate'),
        (2, 2, 'Advanced'),
        (2, 3, 'Expert'),
        -- Question 3: Programming languages
        (3, 0, 'JavaScript'),
        (3, 1, 'Python'),
        (3, 2, 'Rust'),
        (3, 3, 'Solidity'),
        (3, 4, 'Go'),
        (3, 5, 'TypeScript'),
        -- Question 4: Blockchain ecosystems
        (4, 0, 'Ethereum'),
        (4, 1, 'Solana'),
        (4, 2, 'Polkadot'),
        (4, 3, 'Cosmos'),
        (4, 4, 'Avalanche'),
        (4, 5, 'Bitcoin'),
        (4, 6, 'Don''t know'),
        -- Question 5: Project maturity preference
        (5, 0, 'Well-established projects'),
        (5, 1, 'Emerging projects'),
        (5, 2, 'No preference'),
        -- Question 6: Community importance
        (6, 0, 'Very important'),
        (6, 1, 'Somewhat important'),
        (6, 2, 'Not important'),
        -- Question 7: Long-term involvement
        (7, 0, 'Very important'),
        (7, 1, 'Somewhat important'),
        (7, 2, 'Not important')
    ) a(q_index, index, body)
WHERE q.index = a.q_index; 