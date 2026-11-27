package com.antcolonywars.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Default headquarters placement: factions alternate between the left and right edge.
 * The left edge fills top to bottom and the right edge bottom to top, so the first team of each side
 * sits in opposite corners; several teams spread evenly from corner to corner.
 */
public final class HeadquartersLayout {
    private HeadquartersLayout() {
    }

    public static List<Headquarters> place(
            List<Team> teams, int width, int height, int hp, Function<Team, String> idForTeam
    ) {
        Map<String, List<Team>> factions = new LinkedHashMap<>();
        teams.forEach(team -> factions.computeIfAbsent(team.factionId(), key -> new ArrayList<>()).add(team));
        List<List<Team>> sides = List.of(new ArrayList<>(), new ArrayList<>());
        int index = 0;
        for (List<Team> members : factions.values()) {
            sides.get(index++ % 2).addAll(members);
        }
        List<Headquarters> result = new ArrayList<>();
        for (int side = 0; side < 2; side++) {
            List<Team> members = sides.get(side);
            int x = side == 0 ? 0 : width - 1;
            for (int slot = 0; slot < members.size(); slot++) {
                int offset = members.size() == 1
                        ? 0
                        : Math.round(slot * (height - 1) / (float) (members.size() - 1));
                int y = side == 0 ? offset : height - 1 - offset;
                Team team = members.get(slot);
                result.add(new Headquarters(idForTeam.apply(team), team.id(), new Position(x, y), hp));
            }
        }
        return result;
    }
}
