insert into account_user(id, name, created_at, modified_at)
values (1, 'Gil-dong', now(), now());
insert into account_user(id, name, created_at, modified_at)
values (2, 'Cheolsu', now(), now());
insert into account_user(id, name, created_at, modified_at)
values (3, 'Younghee', now(), now());
insert into account(id, created_at, modified_at, account_number, account_status, balance, opened_at, account_user_id)
values (99, now(), now(), '1000000001', 'IN_USE', 10000, now(), 1);
insert into account(id, created_at, modified_at, account_number, account_status, balance, opened_at, account_user_id)
values (100, now(), now(), '1000000002', 'IN_USE', 0, now(), 1);
insert into transaction(id, created_at, modified_at, amount, transacted_at, transaction_id, transaction_result_type, transaction_type, account_id)
values (1, now(), now(), 100, now(), 'transactionId', 'SUCCEED', 'USE', 99);