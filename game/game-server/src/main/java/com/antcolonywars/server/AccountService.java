package com.antcolonywars.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AccountService {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-z0-9_]{3,32}");

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final GameProfileProperties profile;
    private final Clock clock;
    private final Duration sessionDuration;
    private final int maxAccountsPerTeam;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            GameProfileProperties profile,
            Clock clock,
            @Value("${game.security.session-hours:24}") long sessionHours,
            @Value("${game.lobby.max-accounts-per-team:5}") int maxAccountsPerTeam
    ) {
        if (sessionHours < 1) {
            throw new IllegalArgumentException("Session duration must be positive");
        }
        if (maxAccountsPerTeam < 1) {
            throw new IllegalArgumentException("Team account limit must be positive");
        }
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.profile = profile;
        this.clock = clock;
        this.sessionDuration = Duration.ofHours(sessionHours);
        this.maxAccountsPerTeam = maxAccountsPerTeam;
    }

    @Transactional
    public Account register(String username, String displayName, String password) {
        String normalizedUsername = normalizeUsername(username);
        String safeDisplayName = validateDisplayName(displayName);
        validatePassword(password);
        Account account = new Account(
                UUID.randomUUID().toString(), normalizedUsername, safeDisplayName, null
        );
        try {
            jdbcTemplate.update(
                    "INSERT INTO player_account(id, username, normalized_username, display_name, password_hash, created_at_ms) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    account.id(), account.username(), normalizedUsername, account.displayName(),
                    passwordEncoder.encode(password), clock.millis()
            );
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "USERNAME_TAKEN", "Username is already registered");
        }
        return account;
    }

    @Transactional
    public Session login(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        if (password == null) {
            throw invalidCredentials();
        }
        StoredAccount stored = jdbcTemplate.query(
                "SELECT id, username, display_name, password_hash FROM player_account WHERE normalized_username = ?",
                (resultSet, rowNumber) -> new StoredAccount(
                        resultSet.getString("id"),
                        resultSet.getString("username"),
                        resultSet.getString("display_name"),
                        resultSet.getString("password_hash")
                ),
                normalizedUsername
        ).stream().findFirst().orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(password, stored.passwordHash())) {
            throw invalidCredentials();
        }

        long now = clock.millis();
        long expiresAt = now + sessionDuration.toMillis();
        jdbcTemplate.update("DELETE FROM player_session WHERE expires_at_ms <= ?", now);
        String token = createToken();
        jdbcTemplate.update(
                "INSERT INTO player_session(token_hash, account_id, expires_at_ms, created_at_ms) VALUES (?, ?, ?, ?)",
                tokenHash(token), stored.id(), expiresAt, now
        );
        Account account = accountById(stored.id()).orElseThrow(this::invalidCredentials);
        return new Session(token, Instant.ofEpochMilli(expiresAt), account);
    }

    @Transactional
    public Account joinTeam(String token, String teamId) {
        Account account = requireAccount(token);
        String normalizedTeamId = teamId == null ? "" : teamId.trim().toUpperCase(Locale.ROOT);
        Set<String> teamIds = profile.teams().stream().map(GameProfileProperties.TeamProfile::id).collect(java.util.stream.Collectors.toSet());
        if (!teamIds.contains(normalizedTeamId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TEAM_NOT_FOUND", "Team does not exist");
        }
        if (normalizedTeamId.equals(account.teamId())) {
            return account;
        }
        jdbcTemplate.update(
                "MERGE INTO lobby_team_lock(team_id) KEY(team_id) VALUES (?)",
                normalizedTeamId
        );
        jdbcTemplate.queryForObject(
                "SELECT team_id FROM lobby_team_lock WHERE team_id = ? FOR UPDATE",
                String.class,
                normalizedTeamId
        );
        Integer memberCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lobby_member WHERE team_id = ?",
                Integer.class,
                normalizedTeamId
        );
        if (memberCount != null && memberCount >= maxAccountsPerTeam) {
            throw new ApiException(HttpStatus.CONFLICT, "TEAM_FULL", "Team already has the maximum number of accounts");
        }
        int updated = jdbcTemplate.update(
                "UPDATE lobby_member SET team_id = ?, joined_at_ms = ? WHERE account_id = ?",
                normalizedTeamId, clock.millis(), account.id()
        );
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO lobby_member(account_id, team_id, joined_at_ms) VALUES (?, ?, ?)",
                    account.id(), normalizedTeamId, clock.millis()
            );
        }
        return new Account(account.id(), account.username(), account.displayName(), normalizedTeamId);
    }

    public Account requireAccount(String token) {
        return authenticatedAccount(token).orElseThrow(() -> new ApiException(
                HttpStatus.UNAUTHORIZED, "INVALID_SESSION", "Session is missing, expired or invalid"
        ));
    }

    public String resolveTeamId(String token) {
        String demoTeamId = profile.teamIdForToken(token);
        if (demoTeamId != null) {
            return demoTeamId;
        }
        return authenticatedAccount(token).map(Account::teamId).orElse(null);
    }

    private Optional<Account> authenticatedAccount(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return jdbcTemplate.query(
                "SELECT a.id, a.username, a.display_name, lm.team_id "
                        + "FROM player_session s "
                        + "JOIN player_account a ON a.id = s.account_id "
                        + "LEFT JOIN lobby_member lm ON lm.account_id = a.id "
                        + "WHERE s.token_hash = ? AND s.expires_at_ms > ?",
                (resultSet, rowNumber) -> new Account(
                        resultSet.getString("id"),
                        resultSet.getString("username"),
                        resultSet.getString("display_name"),
                        resultSet.getString("team_id")
                ),
                tokenHash(token), clock.millis()
        ).stream().findFirst();
    }

    private Optional<Account> accountById(String accountId) {
        return jdbcTemplate.query(
                "SELECT a.id, a.username, a.display_name, lm.team_id "
                        + "FROM player_account a LEFT JOIN lobby_member lm ON lm.account_id = a.id WHERE a.id = ?",
                (resultSet, rowNumber) -> new Account(
                        resultSet.getString("id"),
                        resultSet.getString("username"),
                        resultSet.getString("display_name"),
                        resultSet.getString("team_id")
                ),
                accountId
        ).stream().findFirst();
    }

    private String normalizeUsername(String username) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_USERNAME",
                    "Username must contain 3 to 32 lowercase letters, numbers or underscores"
            );
        }
        return normalized;
    }

    private String validateDisplayName(String displayName) {
        String value = displayName == null ? "" : displayName.trim();
        if (value.isEmpty() || value.length() > 64) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DISPLAY_NAME", "Display name must contain 1 to 64 characters");
        }
        return value;
    }

    private void validatePassword(String password) {
        int byteLength = password == null ? 0 : password.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength < 8 || byteLength > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", "Password must contain 8 to 72 UTF-8 bytes");
        }
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Username or password is invalid");
    }

    private String createToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String tokenHash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record Account(String id, String username, String displayName, String teamId) {
    }

    public record Session(String token, Instant expiresAt, Account account) {
    }

    private record StoredAccount(String id, String username, String displayName, String passwordHash) {
    }
}
