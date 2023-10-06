ALTER TYPE public.project_visibility RENAME VALUE 'public' TO 'PUBLIC';
ALTER TYPE public.project_visibility RENAME VALUE 'private' TO 'PRIVATE';

ALTER TYPE public.allocated_time RENAME VALUE 'lt1day' to 'less_than_one_day';
ALTER TYPE public.allocated_time RENAME VALUE '1to3days' to 'one_to_three_days';
ALTER TYPE public.allocated_time RENAME VALUE 'gt3days' to 'greater_than_three_days';