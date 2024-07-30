CREATE TYPE user_joining_reasons AS ENUM ('CONTRIBUTOR', 'MAINTAINER');

ALTER TABLE user_profile_info
    ADD COLUMN joining_reason user_joining_reasons DEFAULT 'CONTRIBUTOR'::user_joining_reasons;

UPDATE user_profile_info
SET joining_reason = 'MAINTAINER'
FROM project_leads pl
WHERE id = pl.user_id;

CREATE TYPE user_joining_goals AS ENUM ('LEARN','CHALLENGE','EARN','NOTORIETY');

ALTER TABLE user_profile_info
    ADD COLUMN joining_goal user_joining_goals;

ALTER TABLE user_profile_info
    ADD COLUMN preferred_language_ids uuid[];
ALTER TABLE user_profile_info
    ADD COLUMN preferred_category_ids uuid[];

