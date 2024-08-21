alter table currencies
    add column cmc_id integer;

update currencies
set cmc_id = case
                 when code = 'APT' then 21794
                 when code = 'BTC' then 1
                 when code = 'ETH' then 1027
                 when code = 'EUR' then 2790
                 when code = 'LINK' then 1975
                 when code = 'LORDS' then 17445
                 when code = 'NSTR' then 22743
                 when code = 'OP' then 11840
                 when code = 'STRK' then 22691
                 when code = 'USD' then 2781
                 when code = 'USDC' then 3408
                 when code = 'WLD' then 13502
                 when code = 'XLM' then 512
    end;

alter table currencies
    alter column cmc_id set not null;
