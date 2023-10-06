create table public.auth_users
(
    id                   uuid                  not null
        primary key,
    github_user_id       bigint,
    email                citext,
    last_seen            timestamp,
    login_at_signup      text                  not null,
    avatar_url_at_signup text,
    created_at           timestamp             not null,
    admin                boolean default false not null
);

create unique index auth_users_github_user_id_idx
    on public.auth_users (github_user_id)
    where (github_user_id IS NOT NULL);

create index auth_users_login_at_signup_idx
    on public.auth_users (login_at_signup);


INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('cde93e0e-99cf-4722-8aaa-2c27b91e270d', 112474158, 'tech@onlydust.xyz', '2023-02-01 08:56:06.027000',
        'onlydust-contributor', 'https://avatars.githubusercontent.com/u/112474158?v=4', '2023-02-01 08:56:05.771022',
        false);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('c69f41be-c364-482f-91ae-5e55e3ea719b', 4120347, 'florian.pautot@gmail.com', '2023-02-03 10:32:13.590000',
        'xflpt', 'https://avatars.githubusercontent.com/u/4120347?v=4', '2023-02-03 08:22:10.897654', false);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('4c392643-6d88-400b-a51a-a6e7cad45844', 134487694, 'clement.carouge@pm.me', '2023-05-24 09:45:04.787000',
        'luakili', 'https://avatars.githubusercontent.com/u/134487694?v=4', '2023-05-24 09:45:04.209683', false);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('6115f024-159a-4b1f-b713-1e2ad5c6063e', 4435377, 'bernardstanislas@gmail.com', '2023-04-28 09:13:06.016000',
        'Bernardstanislas', 'https://avatars.githubusercontent.com/u/4435377?v=4', '2022-12-12 09:49:01.083053', false);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('9107596a-e69e-42ce-adc1-0f09dce21bd8', 117683814, 'gregoire.charles@onlydust.xyz',
        '2023-08-07 11:36:57.824000', 'dustyGreg', 'https://avatars.githubusercontent.com/u/117683814?v=4',
        '2023-05-19 09:42:48.274628', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('844e61e9-f96e-4ea5-9fae-ecc6a63d3dde', 133109471, 'gaetan@thedesigncrew.co', '2023-06-30 13:06:46.251000',
        'gaetan-thedesigncrew', 'https://avatars.githubusercontent.com/u/133109471?v=4', '2023-06-06 09:50:04.306548',
        true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('fc92397c-3431-4a84-8054-845376b630a0', 16590657, 'pierre.oucif@gadz.org', '2023-10-04 15:46:16.810000',
        'PierreOucif', 'https://avatars.githubusercontent.com/u/16590657?v=4', '2023-07-22 17:04:19.169178', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('8243910f-863e-4b00-8e9b-7e0f08a07610', 122442765, 'olivier+githubtest3@onlydust.xyz',
        '2023-02-03 17:25:26.540000', 'olivier-dust-3', 'https://avatars.githubusercontent.com/u/122442765?v=4',
        '2023-01-11 10:28:45.472188', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('07c86607-e94d-409c-8214-f67c7ffb9067', 11800019, 'paco.villetard@gmail.com', '2023-10-03 09:24:36.290000',
        'Th3Sc4v3ng3r', 'https://avatars.githubusercontent.com/u/11800019?v=4', '2023-08-30 14:50:54.283669', false);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('4fefe583-ccec-4734-a2c2-90434d3bae55', 138232111, 'gaetan+2@thedesigncrew.co', '2023-07-11 12:42:46.620000',
        'GaetanTDC', 'https://avatars.githubusercontent.com/u/138232111?v=4', '2023-06-30 14:43:39.343888', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('f2215429-83c7-49ce-954b-66ed453c3315', 26790304, 'gaetan.recly@gmail.com', '2023-08-22 14:53:18.061000',
        'gaetanrecly', 'https://avatars.githubusercontent.com/u/26790304?v=4', '2023-01-03 09:45:19.714285', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('44e078b7-d095-49f2-a7b3-647149337dc5', 134493681, 'manonfurst74@gmail.com', '2023-05-29 09:25:29.238000',
        'croziflette74', 'https://avatars.githubusercontent.com/u/134493681?v=4', '2023-05-24 08:59:47.045773', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('dd0ab03c-5875-424b-96db-a35522eab365', 21149076, 'oscar.w.roche@gmail.com', '2023-06-27 09:11:30.869000',
        'oscarwroche', 'https://avatars.githubusercontent.com/u/21149076?v=4', '2022-12-15 08:18:40.237388', false);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('eaa1ddf3-fea5-4cef-825b-336f8e775e05', 5160414, 'haydenclearymusic@gmail.com', '2023-10-05 08:25:44.601000',
        'haydencleary', 'https://avatars.githubusercontent.com/u/5160414?v=4', '2023-10-03 09:06:07.741395', false);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('f20e6812-8de8-432b-9c31-2920434fe7d0', 98735421, 'paco@magicdust.gg', '2023-10-03 09:25:20.330000',
        'pacovilletard', 'https://avatars.githubusercontent.com/u/98735421?v=4', '2023-01-02 10:57:55.202542', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('1df5b5a4-2ee0-481f-8b88-3dd591247623', 10922658, 'alexandrebensimon1@gmail.com', '2023-08-25 14:48:17.973000',
        'alexbensimon', 'https://avatars.githubusercontent.com/u/10922658?v=4', '2023-08-07 13:24:08.806657', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('743e096e-c922-4097-9e6f-8ea503055336', 122993337, 'gregoire@onlydust.xyz', '2023-08-16 13:57:01.631000',
        'GregGamb', 'https://avatars.githubusercontent.com/u/122993337?v=4', '2023-07-11 14:41:51.665318', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('bdc705b5-cf8e-488f-926a-258e1800ed79', 139852598, 'mathias@thedesigncrew.co', '2023-10-05 12:57:05.895000',
        'mat-yas', 'https://avatars.githubusercontent.com/u/139852598?v=4', '2023-07-18 07:30:31.135790', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('a0da3c1e-6493-4ea1-8bd0-8c46d653f274', 31901905, 'chimansky.mickael@gmail.com', '2023-10-05 15:50:14.139000',
        'kaelsky', 'https://avatars.githubusercontent.com/u/31901905?v=4', '2023-09-08 14:35:57.032522', false);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('46fec596-7a91-422e-8532-5f479e790217', 141839618, 'emilie.blum88@gmail.com', '2023-10-05 07:23:47.728000',
        'Blumebee', 'https://avatars.githubusercontent.com/u/141839618?v=4', '2023-09-04 13:14:19.302602', false);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('e461c019-ba23-4671-9b6c-3a5a18748af9', 595505, 'olivier.fuxet@gmail.com', '2023-09-27 08:52:36.037000', 'ofux',
        'https://avatars.githubusercontent.com/u/595505?v=4', '2022-12-12 15:52:23.593172', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('0d29b7bd-9514-4a03-ad14-99bbcbef4733', 142427301, 'kevin@lettria.com', '2023-10-01 16:16:52.185000', 'letkev',
        'https://avatars.githubusercontent.com/u/142427301?v=4', '2023-09-18 15:45:50.175156', false);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('9a779f53-5762-4110-94b8-5596bbbd74ec', 117665867, 'lespiratesfonblos@gmail.com', '2023-09-28 14:29:27.063000',
        'gilbertvandenbruck', 'https://avatars.githubusercontent.com/u/117665867?v=4', '2023-05-19 10:34:30.701155',
        true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('83612081-949a-47c4-a467-6f28f6adad6d', 134486697, 'axel@bconseil.fr', '2023-08-22 15:50:34.695000',
        'axelbconseil', 'https://avatars.githubusercontent.com/u/134486697?v=4', '2023-05-24 09:02:26.970329', false);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('747e663f-4e68-4b42-965b-b5aebedcd4c4', 43467246, 'abuisset@gmail.com', '2023-10-05 19:06:50.034000',
        'AnthonyBuisset', 'https://avatars.githubusercontent.com/u/43467246?v=4', '2022-12-12 09:51:58.485590', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('45e98bf6-25c2-4edf-94da-e340daba8964', 8642470, 'gcm.charles@gmail.com', '2023-10-05 14:10:03.331000',
        'gregcha', 'https://avatars.githubusercontent.com/u/8642470?v=4', '2023-01-02 10:57:58.844811', true);
INSERT INTO public.auth_users (id, github_user_id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at,
                               admin)
VALUES ('f4af340d-6923-453c-bffe-2f1ce1880ff4', 144809540, 'admin@onlydust.xyz', '2023-09-27 12:19:00.474000',
        'CamilleOD', 'https://avatars.githubusercontent.com/u/144809540?v=4', '2023-09-27 12:04:06.149173', false);
