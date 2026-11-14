package com.antcolonywars.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("game.profile")
public record GameProfileProperties(
        String setupId,
        String matchId,
        List<String> matchIds,
        Board board,
        Rules rules,
        List<TeamProfile> teams,
        List<UnitProfile> units,
        List<HeadquartersProfile> headquarters,
        List<ResourceProfile> resources,
        List<BuildingProfile> buildings,
        Limits limits
) {
    public GameProfileProperties {
        List<String> configured = matchIds == null ? List.of() : matchIds;
        matchIds = java.util.stream.Stream.concat(java.util.stream.Stream.of(matchId), configured.stream())
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
    }

    public record Limits(int maxActionsPerBotPerBatch, int commandQueueCapacity, int tickMillis) {
        public Limits {
            if (maxActionsPerBotPerBatch < 1 || commandQueueCapacity < 1 || tickMillis < 1) {
                throw new IllegalArgumentException("Batch, queue and tick limits must be positive");
            }
        }
    }
    public record Board(int width, int height) {
    }

    public record Rules(
            String version,
            int attackDamage,
            int attackRange,
            int maxUnitsPerTeam,
            int spawnedUnitHp,
            int startingResources,
            int harvestAmount,
            int harvestRange,
            int barracksCost,
            int towerCost,
            int wallCost,
            int buildingHp
    ) {
    }

    public record TeamProfile(String id, String displayName, String color, String token, String factionId) {
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

    public record ResourceProfile(String id, int x, int y, int amount) {
    }

    public record BuildingProfile(String id, String teamId, String type, int x, int y, int hp) {
    }

    public String teamIdForToken(String token) {
        return teams.stream()
                .filter(team -> team.token().equals(token))
                .map(TeamProfile::id)
                .findFirst()
                .orElse(null);
    }
}
