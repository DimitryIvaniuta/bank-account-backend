CREATE TABLE account
(
    id         bigint                   not null primary key,
    balance    NUMERIC(19, 2)           NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
