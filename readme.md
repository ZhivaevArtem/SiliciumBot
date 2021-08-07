# Requirements

## Python
3.9.6

## Database
PostgreSQL 13.3

Initialization SQL:
```
CREATE TABLE arr_usernames_config_
(
    usernames_ character varying(31),
    CONSTRAINT arr_usernames_config__pkey PRIMARY KEY (usernames_)
);
CREATE TABLE dic_jokes_config_
(
    key_ character varying(31),
    val_ character varying(255) NOT NULL,
    CONSTRAINT dic_jokes_config__pkey PRIMARY KEY (key_)
);
CREATE TABLE config_
(
    key_       character varying(31),
    int_val_   numeric(31,0),
    str_val_   character varying(255),
    bool_val_  boolean,
    CONSTRAINT config__pkey PRIMARY KEY (key_)
);
INSERT INTO config_(key_, int_val_, str_val_, bool_val_, float_val_)
VALUES
('activity_text',            NULL, '',       NULL, NULL),
('activity_type',            -1,   NULL,     NULL, NULL),
('long_pooling_interval',    300,  NULL,     NULL, NULL),
('long_pooling_query_limit', 5,    NULL,     NULL, NULL),
('messasge_channel_id',      0,    NULL,     NULL, NULL),
('prefix',                   NULL, ';',      NULL, NULL),
('status',                   NULL, 'online', NULL, NULL),
('calculator_timeout',       NULL, NULL,     NULL, 0.5);
```

## Environment variables
**DISCORD_BOT_TOKEN**: discord bot token

**DATABASE_URL**: url to connect to a PostgreSQL database.
URL format:
```postgres://<username>:<password>@<address>:<port>/<database>```

# How to run
```python src/shiki_bot/__main__.py```
