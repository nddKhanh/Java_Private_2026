package com.antcolonywars.server;

import com.antcolonywars.core.Direction;
import com.antcolonywars.core.Headquarters;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.Position;
import com.antcolonywars.core.RuleSet;
import com.antcolonywars.core.Team;
import com.antcolonywars.core.Unit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GameProfileProperties.class)
public class GameServerConfiguration {
    @Bean
    public MatchSetup matchSetup(GameProfileProperties profile) {
        return new MatchSetup(
                profile.setupId(),
                profile.matchId(),
                profile.board().width(),
                profile.board().height(),
                new RuleSet(
                        profile.rules().version(),
                        profile.rules().attackDamage(),
                        profile.rules().attackRange()
                ),
                profile.teams().stream()
                        .map(team -> new Team(team.id(), team.displayName(), team.color()))
                        .toList(),
                profile.units().stream()
                        .map(unit -> new Unit(
                                unit.id(),
                                unit.teamId(),
                                unit.type(),
                                new Position(unit.x(), unit.y()),
                                unit.hp(),
                                Direction.valueOf(unit.facing())
                        ))
                        .toList(),
                profile.headquarters().stream()
                        .map(headquarters -> new Headquarters(
                                headquarters.id(),
                                headquarters.teamId(),
                                new Position(headquarters.x(), headquarters.y()),
                                headquarters.hp()
                        ))
                        .toList()
        );
    }

    @Bean
    public MatchCoordinator matchCoordinator(MatchSetup setup) {
        return new MatchCoordinator(setup);
    }
}
