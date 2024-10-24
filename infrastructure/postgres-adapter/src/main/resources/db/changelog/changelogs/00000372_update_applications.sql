alter table applications
    drop column problem_solving_approach;

alter table applications
    rename column motivations to comment_body;
