ALTER TABLE game_lobby ADD COLUMN pairing_key VARCHAR(6);

UPDATE game_lobby SET pairing_key = match_code;

ALTER TABLE game_lobby ALTER COLUMN pairing_key SET NOT NULL;

CREATE UNIQUE INDEX uq_game_lobby_pairing_key ON game_lobby(pairing_key);
