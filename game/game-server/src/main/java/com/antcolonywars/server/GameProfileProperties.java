package com.antcolonywars.server;

import com.antcolonywars.core.BuildingType;
import com.antcolonywars.core.ResourceType;
import com.antcolonywars.core.RuleSet;
import com.antcolonywars.core.StarterPack;
import com.antcolonywars.core.UnitType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

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
        Limits limits,
        StarterPack starterPack
) {
    public GameProfileProperties {
        List<String> configured = matchIds == null ? List.of() : matchIds;
        matchIds = java.util.stream.Stream.concat(java.util.stream.Stream.of(matchId), configured.stream())
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        units = units == null ? List.of() : units;
        headquarters = headquarters == null ? List.of() : headquarters;
        resources = resources == null ? List.of() : resources;
        buildings = buildings == null ? List.of() : buildings;
        starterPack = starterPack == null ? StarterPack.empty() : starterPack;
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

    /** Balance values loaded from game-rules.yml. */
    public record Rules(
            String version,
            int maxUnitsPerTeam,
            Economy economy,
            HeadquartersRules headquarters,
            Map<UnitType, RuleSet.UnitStats> units,
            Map<BuildingType, RuleSet.BuildingStats> buildings,
            Tower tower
    ) {
        public RuleSet toRuleSet(int tickMillis) {
            return new RuleSet(
                    version, maxUnitsPerTeam, economy.startingWood(), economy.startingMeat(),
                    economy.harvestAmount(), economy.harvestRange(), headquarters.hp(), units, buildings,
                    new RuleSet.TowerStats(tower.damage(), tower.range(),
                            Math.max(1, (int) Math.ceil(tower.cooldownMillis() / (double) tickMillis)))
            );
        }
    }

    public record Economy(int startingWood, int startingMeat, int harvestAmount, int harvestRange) {
    }

    public record HeadquartersRules(int hp) {
    }

    public record Tower(int damage, int range, int cooldownMillis) {
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
            String facing
    ) {
    }

    public record HeadquartersProfile(String id, String teamId, int x, int y) {
    }

    public record ResourceProfile(String id, ResourceType type, int x, int y, int amount) {
    }

    public record BuildingProfile(String id, String teamId, String type, int x, int y) {
    }

    public String teamIdForToken(String token) {
        return teams.stream()
                .filter(team -> team.token().equals(token))
                .map(TeamProfile::id)
                .findFirst()
                .orElse(null);
    }
}
