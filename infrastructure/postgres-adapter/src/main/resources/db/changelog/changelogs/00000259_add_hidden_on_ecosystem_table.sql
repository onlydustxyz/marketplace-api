alter table ecosystems
    add column hidden boolean;

update ecosystems
set hidden = true
where name = 'Aptos';
update ecosystems
set hidden = true
where name = 'Avail';
update ecosystems
set hidden = false
where name = 'Aztec';
update ecosystems
set hidden = false
where name = 'Ethereum';
update ecosystems
set hidden = true
where name = 'Lava';
update ecosystems
set hidden = false
where name = 'Optimism';
update ecosystems
set hidden = false
where name = 'Starknet';
update ecosystems
set hidden = true
where name = 'Zama';

alter table ecosystems
    alter column hidden set not null;