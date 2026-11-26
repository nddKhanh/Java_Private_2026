ALTER TABLE game_lobby_member ADD COLUMN control_token_hash VARCHAR(64);

CREATE UNIQUE INDEX uq_game_lobby_member_control_token ON game_lobby_member(control_token_hash);
