CREATE TYPE accounting.batch_payment_status AS ENUM ('TO_PAY','PAID');

CREATE TABLE accounting.batch_payments
(
    id               uuid primary key,
    csv              text                            not null,
    network          accounting.network              not null,
    status           accounting.batch_payment_status not null default 'TO_PAY',
    transaction_hash text,
    tech_created_at  TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP,
    tech_updated_at  TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.reward_to_batch_payment
(
    batch_payment_id uuid not null,
    reward_id        uuid not null,
    foreign key (batch_payment_id) references accounting.batch_payments (id),
    foreign key (reward_id) references public.payment_requests (id)
);