alter table hackathons
    alter column start_date type timestamp using start_date::timestamp;

alter table hackathons
    alter column end_date type timestamp using start_date::timestamp;
