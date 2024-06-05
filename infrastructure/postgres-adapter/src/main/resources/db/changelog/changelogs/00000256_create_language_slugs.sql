alter table languages
    add column slug text;

update languages
set slug = lower(name);

update languages
set slug = 'cpp'
where name = 'C++';

update languages
set slug = 'csharp'
where name = 'C#';

alter table languages
    alter column slug set not null;

alter table languages
    add constraint languages_slug_key unique (slug);
