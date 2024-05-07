ALTER TYPE iam.backoffice_user_role ADD VALUE 'BO_FINANCIAL_ADMIN';
ALTER TYPE iam.backoffice_user_role ADD VALUE 'BO_MARKETING_ADMIN';

-- New enum values must be committed before they can be used
COMMIT;

update iam.backoffice_users
set roles = '{BO_FINANCIAL_ADMIN,BO_READER}'
where email in ('admin@onlydust.xyz','paco@onlydust.xyz');

update iam.backoffice_users
set roles = '{BO_MARKETING_ADMIN,BO_READER}'
where email in ('alexandre@onlydust.xyz','emilie@onlydust.xyz');

update iam.backoffice_users
set roles = '{BO_READER}'
where 'BO_ADMIN'  = any(roles);

ALTER TYPE iam.backoffice_user_role RENAME TO backoffice_user_role_old;

CREATE TYPE iam.backoffice_user_role AS ENUM ('BO_FINANCIAL_ADMIN','BO_MARKETING_ADMIN', 'BO_READER');

ALTER TABLE iam.backoffice_users ALTER COLUMN roles TYPE iam.backoffice_user_role[] USING roles::text::iam.backoffice_user_role[];

DROP TYPE iam.backoffice_user_role_old;