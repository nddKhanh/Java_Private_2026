package com.antcolonywars.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("game.profile")
public record GameProfileProperties(
        String setupId,
        String matchId,
        Board board,
        Rules rules,
        List<TeamProfile> teams,
        List<UnitProfile> units,
        List<HeadquartersProfile> headquarters
) {
    public record Board(int width, int height) {
    }

    public record Rules(
            String version,
            int attackDamage,
            int attackRange,
            int maxUnitsPerTeam,
            int spawnedUnitHp
    ) {
    }

    public record TeamProfile(String id, String displayName, String color, String token) {
    }

    public record UnitProfile(
            String id,
            String displayName,
            String teamId,
            String type,
            int x,
            int y,
            int hp,
            String facing
    ) {
    }

    public record HeadquartersProfile(String id, String teamId, int x, int y, int hp) {
    }

    public String teamIdForToken(String token) {
        return teams.stream()
                .filter(team -> team.token().equals(token))
                .map(TeamProfile::id)
                .findFirst()
                .orElse(null);
    }
}
