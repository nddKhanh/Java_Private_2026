CREATE TABLE game_lobby (
    match_code VARCHAR(12) PRIMARY KEY,
    owner_account_id VARCHAR(36) NOT NULL,
    setup_json CLOB NOT NULL,
    map_script CLOB NOT NULL,
    status VARCHAR(16) NOT NULL,
    accounts_per_team INT NOT NULL,
    created_at_ms BIGINT NOT NULL,
    started_at_ms BIGINT,
    CONSTRAINT fk_game_lobby_owner FOREIGN KEY (owner_account_id)
        REFERENCES player_account(id) ON DELETE CASCADE
);

CREATE TABLE game_lobby_team (
    match_code VARCHAR(12) NOT NULL,
    team_id VARCHAR(32) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    color VARCHAR(16) NOT NULL,
    team_token VARCHAR(32) NOT NULL,
    PRIMARY KEY (match_code, team_id),
    CONSTRAINT uq_game_lobby_team_token UNIQUE (match_code, team_token),
    CONSTRAINT fk_game_lobby_team_lobby FOREIGN KEY (match_code)
        REFERENCES game_lobby(match_code) ON DELETE CASCADE
);

CREATE TABLE game_lobby_member (
    match_code VARCHAR(12) NOT NULL,
    team_id VARCHAR(32) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    joined_at_ms BIGINT NOT NULL,
    PRIMARY KEY (match_code, account_id),
    CONSTRAINT fk_game_lobby_member_team FOREIGN KEY (match_code, team_id)
        REFERENCES game_lobby_team(match_code, team_id) ON DELETE CASCADE,
    CONSTRAINT fk_game_lobby_member_account FOREIGN KEY (account_id)
        REFERENCES player_account(id) ON DELETE CASCADE
);

CREATE INDEX idx_game_lobby_member_team ON game_lobby_member(match_code, team_id);

