create schema fga;


------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
-- Zanzibar helper functions
------------------------------------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fga.get_relation_user_type(tableName text)
    RETURNS text AS
$$
BEGIN
    RETURN (SELECT column_name
            FROM information_schema.columns
            WHERE table_name = tableName
              AND table_schema = 'fga'
            ORDER BY ordinal_position
            LIMIT 1 OFFSET 1);
END;
$$
    LANGUAGE plpgsql;


create or replace function fga.new_relation(type text, name text, userType text) returns void as
$$
declare
    tableName  text;
    getterName text;
begin
    tableName := format('%s_%ss', type, name);
    getterName := format('%s', name);
    execute format(
            'create table fga.%s (
                "%s" fga.%s,
                "%s" fga.%s,
                primary key ("%s", "%s")
            );',
            tableName,
            type,
            type,
            userType,
            userType,
            type,
            userType
            );
    execute format(
            'create unique index %s_pkey_inv on fga.%s ("%s", "%s");',
            tableName,
            tableName,
            userType,
            type
            );
    execute format(
            'create or replace function fga.%s(_%s fga.%s, _%s fga.%s) returns boolean as $func$
                select exists(
                    select 1
                    from fga.%s
                    where "%s" = _%s and "%s" = _%s
                )
            $func$ language sql stable;',
            getterName,
            type,
            type,
            userType,
            userType,
            tableName,
            type,
            type,
            userType,
            userType
            );
    execute format(
            'create or replace function fga.add_%ss(_%s fga.%s, _%ss fga.%s[]) returns void as $func$
                insert into fga.%s
                select _%s, unnest(_%ss)
                on conflict do nothing;
            $func$ language sql;',
            name,
            type,
            type,
            userType,
            userType,
            tableName,
            type,
            userType
            );
    execute format(
            'create or replace function fga.remove_%ss(_%s fga.%s, _%ss fga.%s[]) returns void as $func$
                delete from fga.%s where "%s" = _%s and "%s" = any(_%ss);
            $func$ language sql;',
            name,
            type,
            type,
            userType,
            userType,
            tableName,
            type,
            type,
            userType,
            userType
            );
    execute format(
            'create or replace function fga.override_%ss(_%s fga.%s, _%ss fga.%s[]) returns void as $func$
                delete from fga.%s where "%s" = _%s and "%s" != any(_%ss);
                select fga.add_%ss(_%s, _%ss);
            $func$ language sql;',
            name,
            type,
            type,
            userType,
            userType,
            tableName,
            type,
            type,
            userType,
            userType,
            name,
            type,
            userType
            );
    raise notice 'Created table fga.%', tableName;
    raise notice 'Created function: fga.%', getterName;
    raise notice 'Created function: fga.add_%s', name;
    raise notice 'Created function: fga.remove_%s', name;
    raise notice 'Created function: fga.override_%s', name;
end;
$$ language plpgsql;



create or replace function fga.new_implied_relation(
    type text,
    name text,
    expression text
) returns void as
$$
declare
    sql_parts             text[];
    expr_parts            text[];
    current_part          text;
    from_parts            text[];
    result_sql            text;
    related_relation_name text;
begin
    -- Split expression by 'or'
    expr_parts := string_to_array(expression, ' or ');
    sql_parts := array []::text[];

    -- Process each part
    foreach current_part in array expr_parts
        loop
            if position(' from ' in current_part) > 0 then
                -- Handle "X from Y" expressions
                from_parts := string_to_array(current_part, ' from ');
                related_relation_name := format('%s_%ss', type, from_parts[2]);
                sql_parts := array_append(sql_parts, format(
                        'exists(select 1 from fga.%s r where r.%s = _%s and fga.%s(r.%s, _user))',
                        related_relation_name, -- related entity type
                        type, -- foreign key column
                        type, -- parameter name
                        from_parts[1], -- related relation name
                        fga.get_relation_user_type(related_relation_name)));
            else
                -- Handle direct relations
                sql_parts := array_append(sql_parts, format(
                        'fga.%s(_%s, _user)',
                        current_part,
                        type));
            end if;
        end loop;

    -- Construct the complete function
    result_sql := format(
            'create or replace function fga.%s(_%s fga.%s, _user fga.user) returns boolean as $func$
                select %s
            $func$ language sql stable;',
            name,
            type,
            type,
            array_to_string(sql_parts, E'\n       or '));

    -- Create the function
    execute result_sql;

    raise notice 'Created implied relation function: fga.%', name;
end;
$$ language plpgsql;



------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
-- Zanzibar model
------------------------------------------------------------------------------------------------------------------------

-- Types
create domain fga.user as uuid not null;
create domain fga.ecosystem as uuid not null;
create domain fga.sponsor as uuid not null;
create domain fga.program as uuid not null;
create domain fga.project as uuid not null;
create domain fga.billing_profile as uuid not null;
create domain fga.reward as uuid not null;

-- Ecosystem direct relations
select fga.new_relation('ecosystem', 'head_of', 'user');

-- Sponsor direct relations
select fga.new_relation('sponsor', 'head_of_finance', 'user');
select fga.new_relation('sponsor', 'finance_viewer', 'user');
select fga.new_relation('sponsor', 'ecosystem', 'ecosystem');

-- Program direct relations
select fga.new_relation('program', 'lead', 'user');
select fga.new_relation('program', 'allocating_sponsor', 'sponsor');

-- Project direct relations
select fga.new_relation('project', 'maintainer', 'user');
select fga.new_relation('project', 'granting_program', 'program');

-- Billing profile direct relations
select fga.new_relation('billing_profile', 'admin', 'user');
select fga.new_relation('billing_profile', 'member', 'user');

-- Reward direct relations
select fga.new_relation('reward', 'project', 'project');
select fga.new_relation('reward', 'recipient', 'user');


-- Sponsor implied relations
select fga.new_implied_relation('sponsor', 'can_edit', 'head_of_finance');
select fga.new_implied_relation('sponsor', 'can_read_financial', 'finance_viewer or head_of_finance or head_of from ecosystem');
select fga.new_implied_relation('sponsor', 'can_send_allocation', 'head_of_finance');
select fga.new_implied_relation('sponsor', 'can_deposit_funds', 'head_of_finance');
select fga.new_implied_relation('sponsor', 'can_edit_a_program', 'head_of_finance');

-- Program implied relations
select fga.new_implied_relation('project', 'can_edit', 'lead or can_edit_a_program from allocating_sponsor');
select fga.new_implied_relation('program', 'can_edit_permissions', 'lead or can_edit_a_program from allocating_sponsor');
select fga.new_implied_relation('program', 'can_read_financial', 'lead or can_read_financial from allocating_sponsor');
select fga.new_implied_relation('program', 'can_refund_allocation', 'lead');
select fga.new_implied_relation('program', 'can_send_grant', 'lead');

-- Project implied relations
select fga.new_implied_relation('project', 'can_edit', 'maintainer');
select fga.new_implied_relation('project', 'can_edit_permissions', 'maintainer');
select fga.new_implied_relation('project', 'can_read_financial', 'maintainer or can_read_financial from granting_program');
select fga.new_implied_relation('project', 'can_refund_grant', 'maintainer');
select fga.new_implied_relation('project', 'can_send_reward', 'maintainer');

-- Billing profile implied relations
select fga.new_implied_relation('billing_profile', 'can_read', 'admin or member');
select fga.new_implied_relation('billing_profile', 'can_write', 'admin');

-- Reward implied relations
select fga.new_implied_relation('reward', 'can_cancel', 'maintainer from project');
select fga.new_implied_relation('reward', 'can_claim', 'recipient');



------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
-- Migrate existing permissions
------------------------------------------------------------------------------------------------------------------------

select fga.add_maintainers(pl.project_id::fga.project, array_agg(pl.user_id::fga.user))
from project_leads pl
group by pl.project_id;

