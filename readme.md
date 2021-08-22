# Requirements

## Python
3.9.6

## Database
PostgreSQL 13.3

Initialization SQL:
```
CREATE TABLE arr_str_usernames_botdata_
(
    val_ character varying(31),
    CONSTRAINT arr_usernames_config__pkey PRIMARY KEY (usernames_)
);
CREATE TABLE dic_str_str_jokes_botdata_
(
    key_ character varying(31),
    val_ character varying(255) NOT NULL,
    CONSTRAINT dic_jokes_config__pkey PRIMARY KEY (key_)
);
CREATE TABLE config_botdata_
(
    key_       character varying(31),
    int_val_   numeric(31,0),
    str_val_   character varying(255),
    bool_val_  boolean,
    float_val_ real,
    CONSTRAINT config__pkey PRIMARY KEY (key_)
);
INSERT INTO config_botdata_(key_, int_val_, str_val_, bool_val_, float_val_)
VALUES
('activity_text',            NULL, '',       NULL, NULL),
('activity_type',            -1,   NULL,     NULL, NULL),
('calculator_timeout',       NULL, NULL,     NULL, 0.5),
('history_request_limit',    5,    NULL,     NULL, NULL),
('is_worker_running',        NULL, NULL,     true, NULL),
('loop_requests_interval',   300,  NULL,     NULL, NULL),
('messasge_channel_id',      0,    NULL,     NULL, NULL),
('prefix',                   NULL, ';',      NULL, NULL),
('status',                   NULL, 'online', NULL, NULL);
```

## Environment variables
**DISCORD_BOT_TOKEN**: discord bot token

**DATABASE_URL**: url to connect to a PostgreSQL database.
URL format:
```postgres://<username>:<password>@<address>:<port>/<database>```

# How to run
```python src/shiki_bot/__main__.py```
