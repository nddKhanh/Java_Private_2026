package com.antcolonywars.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeadquartersLayoutTest {
    @Test
    void placesFactionsOnOppositeEdgesFromCornerToCorner() {
        List<Team> teams = new ArrayList<>();
        for (int index = 1; index <= 10; index++) {
            teams.add(new Team("T" + index, "Team " + index, "#000000", index <= 5 ? "A" : "B"));
        }

        List<Headquarters> headquarters = HeadquartersLayout.place(teams, 50, 50, 3, team -> "hq-" + team.id());
        Map<String, Position> byTeam = headquarters.stream()
                .collect(Collectors.toMap(Headquarters::teamId, Headquarters::position));

        assertEquals(10, new HashSet<>(byTeam.values()).size());
        assertEquals(List.of(0, 12, 25, 37, 49), List.of("T1", "T2", "T3", "T4", "T5").stream()
                .map(id -> byTeam.get(id).y()).toList());
        assertEquals(new Position(0, 0), byTeam.get("T1"));
        assertEquals(new Position(49, 49), byTeam.get("T6"));
        assertEquals(new Position(49, 0), byTeam.get("T10"));
        headquarters.forEach(hq -> assertEquals(3, hq.hp()));
    }

    @Test
    void putsSingleTeamFactionsInOppositeCorners() {
        List<Headquarters> headquarters = HeadquartersLayout.place(List.of(
                new Team("A", "A", "#000000", "A"), new Team("B", "B", "#000000", "B")
        ), 12, 12, 3, Team::id);

        assertEquals(new Position(0, 0), headquarters.get(0).position());
        assertEquals(new Position(11, 11), headquarters.get(1).position());
    }
}
