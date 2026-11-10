CREATE TABLE player_account (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    normalized_username VARCHAR(32) NOT NULL UNIQUE,
    display_name VARCHAR(64) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at_ms BIGINT NOT NULL
);

CREATE TABLE player_session (
    token_hash VARCHAR(64) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    expires_at_ms BIGINT NOT NULL,
    created_at_ms BIGINT NOT NULL,
    CONSTRAINT fk_player_session_account
        FOREIGN KEY (account_id) REFERENCES player_account(id) ON DELETE CASCADE
);

CREATE INDEX idx_player_session_account ON player_session(account_id);

CREATE TABLE lobby_member (
    account_id VARCHAR(36) PRIMARY KEY,
    team_id VARCHAR(32) NOT NULL,
    joined_at_ms BIGINT NOT NULL,
    CONSTRAINT fk_lobby_member_account
        FOREIGN KEY (account_id) REFERENCES player_account(id) ON DELETE CASCADE
);

CREATE INDEX idx_lobby_member_team ON lobby_member(team_id);

CREATE TABLE match_snapshot (
    match_id VARCHAR(64) PRIMARY KEY,
    state_json CLOB NOT NULL,
    updated_at_ms BIGINT NOT NULL
);

CREATE TABLE match_event (
    match_id VARCHAR(64) NOT NULL,
    event_sequence BIGINT NOT NULL,
    event_json CLOB NOT NULL,
    created_at_ms BIGINT NOT NULL,
    PRIMARY KEY (match_id, event_sequence)
);
