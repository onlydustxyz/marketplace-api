alter table committee_project_questions
    add column rank int;

with ranked_questions as (select row_number() over (partition by committee_id) as rank, id from committee_project_questions)
update committee_project_questions cpq
set rank = rq.rank
from ranked_questions rq
where rq.id = cpq.id;

-- alter table committee_project_questions
--     alter column rank set not null;

create index idx_committee_project_questions_committee_id_rank
    on committee_project_questions (committee_id, rank);



alter table committee_jury_criteria
    add column rank int;

with ranked_criteria as (select row_number() over (partition by committee_id) as rank, id from committee_jury_criteria)
update committee_jury_criteria cpq
set rank = rq.rank
from ranked_criteria rq
where rq.id = cpq.id;

-- alter table committee_jury_criteria
--     alter column rank set not null;

create index idx_committee_jury_criteria_committee_id_rank
    on committee_jury_criteria (committee_id, rank);
