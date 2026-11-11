package com.antcolonywars.server;

import com.antcolonywars.core.Direction;
import com.antcolonywars.core.Headquarters;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.MatchFactory;
import com.antcolonywars.core.Position;
import com.antcolonywars.core.ResourceNode;
import com.antcolonywars.core.Team;
import com.antcolonywars.core.Unit;
import com.antcolonywars.core.UnitType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class MapScriptParser {
    private static final Pattern MAP_PATTERN = Pattern.compile("(\\d+)[xX](\\d+)");
    private static final List<String> COLORS = List.of(
            "#2563eb", "#dc2626", "#16a34a", "#ca8a04", "#c026d3",
            "#06b6d4", "#ea580c", "#0d9488", "#7f1d1d", "#1e3a8a"
    );

    private final ObjectMapper objectMapper;
    private final MatchSetup template;

    public MapScriptParser(ObjectMapper objectMapper, MatchSetup template) {
        this.objectMapper = objectMapper;
        this.template = template;
    }

    public ParsedMap parse(String matchCode, String source) {
        MapScript script;
        try {
            script = objectMapper.readValue(source == null ? "" : source, MapScript.class);
        } catch (JsonProcessingException exception) {
            throw invalid("Map script must be valid JSON");
        }
        Matcher matcher = MAP_PATTERN.matcher(text(script.map()));
        if (!matcher.matches() || !matcher.group(1).equals(matcher.group(2))) {
            throw invalid("map must use the square format nxn, for example 12x12");
        }
        int size = Integer.parseInt(matcher.group(1));
        int teamCount = value(script.teams());
        int accountsPerTeam = value(script.accountsPerTeam());
        if (size < 6 || size > 40) {
            throw invalid("Map size must be between 6x6 and 40x40");
        }
        if (teamCount < 2 || teamCount > 10) {
            throw invalid("teams must be between 2 and 10");
        }
        if (accountsPerTeam < 1 || accountsPerTeam > 5) {
            throw invalid("accountsPerTeam must be between 1 and 5");
        }

        List<Team> teams = new ArrayList<>();
        for (int index = 1; index <= teamCount; index++) {
            teams.add(new Team(teamId(index), "Team " + index, COLORS.get(index - 1), teamId(index)));
        }

        Set<String> occupied = new HashSet<>();
        Set<Integer> headquartersTeams = new HashSet<>();
        List<Headquarters> headquarters = new ArrayList<>();
        int hqIndex = 0;
        for (HeadquartersSpec spec : list(script.headquarters())) {
            validateTeam(spec.team(), teamCount);
            if (!headquartersTeams.add(spec.team())) {
                throw invalid("Each team must have exactly one headquarters");
            }
            Position position = position(spec.x(), spec.y(), size, occupied);
            headquarters.add(new Headquarters("hq-" + (++hqIndex), teamId(spec.team()), position, 3));
        }
        if (headquartersTeams.size() != teamCount) {
            throw invalid("headquarters must contain one entry for every team");
        }

        Set<Integer> unitTeams = new HashSet<>();
        List<Unit> units = new ArrayList<>();
        int unitIndex = 0;
        for (UnitSpec spec : list(script.units())) {
            validateTeam(spec.team(), teamCount);
            unitTeams.add(spec.team());
            Position position = position(spec.x(), spec.y(), size, occupied);
            String type = text(spec.type()).isBlank()
                    ? UnitType.WORKER.name()
                    : text(spec.type()).toUpperCase(Locale.ROOT);
            try {
                UnitType.valueOf(type);
            } catch (IllegalArgumentException exception) {
                throw invalid("Unsupported unit type: " + type);
            }
            Direction facing = position.x() < size / 2 ? Direction.RIGHT : Direction.LEFT;
            units.add(new Unit(
                    "unit-" + (++unitIndex), "Unit " + unitIndex, teamId(spec.team()), type, position, 3, facing
            ));
        }
        if (unitTeams.size() != teamCount) {
            throw invalid("units must contain at least one entry for every team");
        }

        List<ResourceNode> resources = new ArrayList<>();
        int resourceIndex = 0;
        for (ResourceSpec spec : list(script.resources())) {
            if (spec.amount() < 1) {
                throw invalid("Resource amount must be positive");
            }
            resources.add(new ResourceNode(
                    "resource-" + (++resourceIndex), position(spec.x(), spec.y(), size, occupied), spec.amount()
            ));
        }

        MatchSetup setup = new MatchSetup(
                "custom-" + matchCode, matchCode, size, size, template.rules(),
                teams, units, headquarters, resources, List.of()
        );
        try {
            MatchFactory.create(setup);
        } catch (IllegalArgumentException exception) {
            throw invalid(exception.getMessage());
        }
        return new ParsedMap(setup, accountsPerTeam);
    }

    private Position position(int x, int y, int size, Set<String> occupied) {
        if (x < 0 || y < 0 || x >= size || y >= size) {
            throw invalid("All positions must be inside the map");
        }
        if (!occupied.add(x + ":" + y)) {
            throw invalid("Two initial objects cannot share position (" + x + ", " + y + ")");
        }
        return new Position(x, y);
    }

    private void validateTeam(int team, int teamCount) {
        if (team < 1 || team > teamCount) {
            throw invalid("Team numbers must be between 1 and " + teamCount);
        }
    }

    private String teamId(int number) {
        return "TEAM_" + number;
    }

    private int value(Integer number) {
        return number == null ? 0 : number;
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private <T> List<T> list(List<T> value) {
        return value == null ? List.of() : value;
    }

    private ApiException invalid(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MAP_SCRIPT", message);
    }

    public record ParsedMap(MatchSetup setup, int accountsPerTeam) {
    }

    public record MapScript(
            String map,
            Integer teams,
            Integer accountsPerTeam,
            List<ResourceSpec> resources,
            List<HeadquartersSpec> headquarters,
            List<UnitSpec> units
    ) {
    }

    public record ResourceSpec(int x, int y, int amount) {
    }

    public record HeadquartersSpec(int team, int x, int y) {
    }

    public record UnitSpec(int team, int x, int y, String type) {
    }
}
