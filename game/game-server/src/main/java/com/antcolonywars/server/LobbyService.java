package com.antcolonywars.server;

import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.Team;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class LobbyService {
    private static final char[] CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final char[] PAIRING_KEY_ALPHABET = "23456789".toCharArray();
    private static final List<String> TEAM_TOKENS = List.of(
            "BLUE", "RED", "GREEN", "YELLOW", "MAGENTA",
            "CYAN", "ORANGE", "TEAL", "MAROON", "NAVY"
    );

    private final JdbcTemplate jdbcTemplate;
    private final AccountService accountService;
    private final MapScriptParser mapScriptParser;
    private final MatchRegistry matchRegistry;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public LobbyService(
            JdbcTemplate jdbcTemplate,
            AccountService accountService,
            MapScriptParser mapScriptParser,
            MatchRegistry matchRegistry,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.accountService = accountService;
        this.mapScriptParser = mapScriptParser;
        this.matchRegistry = matchRegistry;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void restoreStartedMatches() {
        jdbcTemplate.query(
                "SELECT setup_json FROM game_lobby WHERE status = 'RUNNING'",
                (resultSet, rowNumber) -> readSetup(resultSet.getString("setup_json"))
        ).forEach(matchRegistry::register);
    }

    @Transactional
    public LobbyView create(String sessionToken, String mapScript) {
        AccountService.Account account = accountService.requireAccount(sessionToken);
        String matchCode = createUniqueCode();
        String pairingKey = createUniquePairingKey();
        MapScriptParser.ParsedMap parsed = mapScriptParser.parse(matchCode, mapScript);
        MatchSetup setup = parsed.setup();
        jdbcTemplate.update(
                "INSERT INTO game_lobby(match_code, pairing_key, owner_account_id, setup_json, map_script, status, accounts_per_team, created_at_ms) "
                        + "VALUES (?, ?, ?, ?, ?, 'WAITING', ?, ?)",
                matchCode, pairingKey, account.id(), writeSetup(setup), mapScript,
                parsed.accountsPerTeam(), clock.millis()
        );
        for (int index = 0; index < setup.teams().size(); index++) {
            Team team = setup.teams().get(index);
            jdbcTemplate.update(
                    "INSERT INTO game_lobby_team(match_code, team_id, display_name, color, team_token) VALUES (?, ?, ?, ?, ?)",
                    matchCode, team.id(), team.displayName(), team.color(), TEAM_TOKENS.get(index)
            );
        }
        jdbcTemplate.update(
                "INSERT INTO game_lobby_member(match_code, team_id, account_id, joined_at_ms) VALUES (?, ?, ?, ?)",
                matchCode, setup.teams().getFirst().id(), account.id(), clock.millis()
        );
        return viewFor(account, matchCode);
    }

    public LobbyView view(String sessionToken, String matchCode) {
        return viewFor(accountService.requireAccount(sessionToken), normalizeCode(matchCode));
    }

    @Transactional
    public LobbyView join(String sessionToken, String matchCode, String teamToken) {
        AccountService.Account account = accountService.requireAccount(sessionToken);
        String code = normalizeCode(matchCode);
        LobbyRow lobby = lockLobby(code);
        if (!"WAITING".equals(lobby.status())) {
            throw new ApiException(HttpStatus.CONFLICT, "MATCH_ALREADY_STARTED", "The match has already started");
        }
        String normalizedTeamToken = teamToken == null ? "" : teamToken.trim().toUpperCase(Locale.ROOT);
        TeamRow team = jdbcTemplate.query(
                "SELECT team_id, display_name, color, team_token FROM game_lobby_team WHERE match_code = ? AND team_token = ?",
                (resultSet, rowNumber) -> new TeamRow(
                        resultSet.getString("team_id"), resultSet.getString("display_name"),
                        resultSet.getString("color"), resultSet.getString("team_token")
                ),
                code, normalizedTeamToken
        ).stream().findFirst().orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "TEAM_TOKEN_NOT_FOUND", "Match code or team token is incorrect"
        ));

        Optional<String> currentTeam = jdbcTemplate.query(
                "SELECT team_id FROM game_lobby_member WHERE match_code = ? AND account_id = ?",
                (resultSet, rowNumber) -> resultSet.getString("team_id"), code, account.id()
        ).stream().findFirst();
        if (currentTeam.filter(team.teamId()::equals).isPresent()) {
            return viewFor(account, code);
        }
        Integer memberCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM game_lobby_member WHERE match_code = ? AND team_id = ?",
                Integer.class, code, team.teamId()
        );
        if (memberCount != null && memberCount >= lobby.accountsPerTeam()) {
            throw new ApiException(HttpStatus.CONFLICT, "TEAM_FULL", "This team is already full");
        }
        if (currentTeam.isPresent()) {
            jdbcTemplate.update(
                    "UPDATE game_lobby_member SET team_id = ?, joined_at_ms = ? WHERE match_code = ? AND account_id = ?",
                    team.teamId(), clock.millis(), code, account.id()
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO game_lobby_member(match_code, team_id, account_id, joined_at_ms) VALUES (?, ?, ?, ?)",
                    code, team.teamId(), account.id(), clock.millis()
            );
        }
        return viewFor(account, code);
    }

    @Transactional
    public LobbyView start(String sessionToken, String matchCode) {
        AccountService.Account account = accountService.requireAccount(sessionToken);
        String code = normalizeCode(matchCode);
        LobbyRow lobby = lockLobby(code);
        if (!lobby.ownerAccountId().equals(account.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_MATCH_OWNER", "Only the match owner can start it");
        }
        if ("WAITING".equals(lobby.status())) {
            matchRegistry.register(readSetup(lobby.setupJson()));
            jdbcTemplate.update(
                    "UPDATE game_lobby SET status = 'RUNNING', started_at_ms = ? WHERE match_code = ?",
                    clock.millis(), code
            );
        }
        return viewFor(account, code);
    }

    public String resolveTeamId(String matchId, String token) {
        if (matchId == null || token == null || token.isBlank()) {
            return null;
        }
        String code = matchId.trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z2-9]{6}")) {
            return null;
        }
        Optional<String> teamByToken = jdbcTemplate.query(
                "SELECT team_id FROM game_lobby_team WHERE match_code = ? AND team_token = ?",
                (resultSet, rowNumber) -> resultSet.getString("team_id"),
                code, token.trim().toUpperCase(Locale.ROOT)
        ).stream().findFirst();
        if (teamByToken.isPresent()) {
            return teamByToken.get();
        }
        return accountService.findAccount(token).flatMap(account -> jdbcTemplate.query(
                "SELECT team_id FROM game_lobby_member WHERE match_code = ? AND account_id = ?",
                (resultSet, rowNumber) -> resultSet.getString("team_id"), code, account.id()
        ).stream().findFirst()).orElse(null);
    }

    public PairingView pair(String pairingKey, String serverHost, int serverPort) {
        String key = pairingKey == null ? "" : pairingKey.trim().toUpperCase(Locale.ROOT);
        if (!key.matches("[A-Z2-9]{6}")) {
            throw pairingNotFound();
        }
        LobbyRow lobby = jdbcTemplate.query(
                "SELECT match_code, pairing_key, owner_account_id, setup_json, status, accounts_per_team "
                        + "FROM game_lobby WHERE pairing_key = ?",
                (resultSet, rowNumber) -> lobbyRow(resultSet), key
        ).stream().findFirst().orElseThrow(this::pairingNotFound);
        List<PairingTeam> teams = jdbcTemplate.query(
                "SELECT team_id, display_name, team_token FROM game_lobby_team WHERE match_code = ? ORDER BY team_id",
                (resultSet, rowNumber) -> new PairingTeam(
                        resultSet.getString("team_id"), resultSet.getString("display_name"),
                        resultSet.getString("team_token")
                ),
                lobby.matchCode()
        );
        String host = serverHost.contains(":") ? "[" + serverHost + "]" : serverHost;
        return new PairingView(
                "ws://" + host + ":" + serverPort + "/game",
                lobby.matchCode(), lobby.status(), teams
        );
    }

    private LobbyView viewFor(AccountService.Account account, String matchCode) {
        LobbyRow lobby = requireLobby(matchCode);
        boolean member = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM game_lobby_member WHERE match_code = ? AND account_id = ?",
                Integer.class, matchCode, account.id()
        ) > 0;
        if (!member) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_IN_MATCH", "Join the match before opening its lobby");
        }
        List<TeamView> teams = jdbcTemplate.query(
                "SELECT team_id, display_name, color, team_token FROM game_lobby_team WHERE match_code = ? ORDER BY team_id",
                (resultSet, rowNumber) -> {
                    String teamId = resultSet.getString("team_id");
                    List<MemberView> members = jdbcTemplate.query(
                            "SELECT a.id, a.username FROM game_lobby_member lm JOIN player_account a ON a.id = lm.account_id "
                                    + "WHERE lm.match_code = ? AND lm.team_id = ? ORDER BY lm.joined_at_ms",
                            (memberSet, memberRow) -> new MemberView(
                                    memberSet.getString("id"), memberSet.getString("username")
                            ),
                            matchCode, teamId
                    );
                    return new TeamView(
                            teamId, resultSet.getString("display_name"), resultSet.getString("color"),
                            resultSet.getString("team_token"), lobby.accountsPerTeam(), members
                    );
                },
                matchCode
        );
        return new LobbyView(
                lobby.matchCode(), lobby.pairingKey(), lobby.status(), account.id(),
                lobby.ownerAccountId().equals(account.id()), teams
        );
    }

    private LobbyRow lockLobby(String matchCode) {
        return jdbcTemplate.query(
                "SELECT match_code, pairing_key, owner_account_id, setup_json, status, accounts_per_team FROM game_lobby "
                        + "WHERE match_code = ? FOR UPDATE",
                (resultSet, rowNumber) -> lobbyRow(resultSet), matchCode
        ).stream().findFirst().orElseThrow(() -> notFound());
    }

    private LobbyRow requireLobby(String matchCode) {
        return jdbcTemplate.query(
                "SELECT match_code, pairing_key, owner_account_id, setup_json, status, accounts_per_team "
                        + "FROM game_lobby WHERE match_code = ?",
                (resultSet, rowNumber) -> lobbyRow(resultSet), matchCode
        ).stream().findFirst().orElseThrow(() -> notFound());
    }

    private LobbyRow lobbyRow(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        return new LobbyRow(
                resultSet.getString("match_code"), resultSet.getString("pairing_key"),
                resultSet.getString("owner_account_id"),
                resultSet.getString("setup_json"), resultSet.getString("status"),
                resultSet.getInt("accounts_per_team")
        );
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "MATCH_NOT_FOUND", "Match code was not found");
    }

    private ApiException pairingNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "PAIRING_KEY_NOT_FOUND", "Pairing key was not found");
    }

    private String normalizeCode(String matchCode) {
        String code = matchCode == null ? "" : matchCode.trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z2-9]{6}")) {
            throw notFound();
        }
        return code;
    }

    private String createUniqueCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String code = createCode(6);
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM game_lobby WHERE match_code = ?", Integer.class, code
            );
            if (count != null && count == 0) {
                return code;
            }
        }
        throw new IllegalStateException("Could not allocate a unique match code");
    }

    private String createUniquePairingKey() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String key = createCode(6, PAIRING_KEY_ALPHABET);
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM game_lobby WHERE pairing_key = ?", Integer.class, key
            );
            if (count != null && count == 0) {
                return key;
            }
        }
        throw new IllegalStateException("Could not allocate a unique pairing key");
    }

    private String createCode(int length) {
        return createCode(length, CODE_ALPHABET);
    }

    private String createCode(int length, char[] alphabet) {
        StringBuilder result = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            result.append(alphabet[secureRandom.nextInt(alphabet.length)]);
        }
        return result.toString();
    }

    private String writeSetup(MatchSetup setup) {
        try {
            return objectMapper.writeValueAsString(setup);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize map setup", exception);
        }
    }

    private MatchSetup readSetup(String json) {
        try {
            return objectMapper.readValue(json, MatchSetup.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot deserialize map setup", exception);
        }
    }

    private record LobbyRow(
            String matchCode, String pairingKey, String ownerAccountId, String setupJson,
            String status, int accountsPerTeam
    ) {
    }

    private record TeamRow(String teamId, String displayName, String color, String teamToken) {
    }

    public record MemberView(String accountId, String username) {
    }

    public record TeamView(
            String teamId, String displayName, String color, String token, int capacity, List<MemberView> members
    ) {
    }

    public record LobbyView(
            String matchCode, String pairingKey, String status, String currentAccountId,
            boolean owner, List<TeamView> teams
    ) {
    }

    public record PairingTeam(String teamId, String displayName, String token) {
    }

    public record PairingView(
            String serverUrl, String matchCode, String status, List<PairingTeam> teams
    ) {
    }
}
