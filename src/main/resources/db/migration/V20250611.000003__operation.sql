CREATE TABLE operation
(
    id             bigint                   not null primary key,
    account_id     BIGINT                   NOT NULL REFERENCES account (id),
    type           SMALLINT                 NOT NULL default 0,
    amount         NUMERIC(19, 2)           NOT NULL,
    operation_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    balance_after  NUMERIC(19, 2)           NOT NULL
);