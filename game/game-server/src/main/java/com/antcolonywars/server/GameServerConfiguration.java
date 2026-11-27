package com.antcolonywars.server;

import com.antcolonywars.core.Direction;
import com.antcolonywars.core.Building;
import com.antcolonywars.core.BuildingType;
import com.antcolonywars.core.Headquarters;
import com.antcolonywars.core.HeadquartersLayout;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.Position;
import com.antcolonywars.core.RuleSet;
import com.antcolonywars.core.StarterPack;
import com.antcolonywars.core.Team;
import com.antcolonywars.core.Unit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;
import java.util.List;
import java.util.Locale;

@Configuration
@EnableConfigurationProperties(GameProfileProperties.class)
public class GameServerConfiguration {
    @Bean
    public StarterPack starterPack(GameProfileProperties profile) {
        return profile.starterPack();
    }

    @Bean
    public MatchSetup matchSetup(GameProfileProperties profile, StarterPack starterPack) {
        RuleSet rules = profile.rules().toRuleSet(profile.limits().tickMillis());
        List<Team> teams = profile.teams().stream()
                .map(team -> new Team(team.id(), team.displayName(), team.color(), team.factionId()))
                .toList();
        List<Headquarters> headquarters = profile.headquarters().isEmpty()
                ? HeadquartersLayout.place(teams, profile.board().width(), profile.board().height(),
                        rules.headquartersHp(), team -> "hq-" + team.id().toLowerCase(Locale.ROOT))
                : profile.headquarters().stream()
                        .map(item -> new Headquarters(
                                item.id(), item.teamId(), new Position(item.x(), item.y()), rules.headquartersHp()
                        ))
                        .toList();
        return starterPack.applyTo(new MatchSetup(
                profile.setupId(),
                profile.matchId(),
                profile.board().width(),
                profile.board().height(),
                rules,
                teams,
                profile.units().stream()
                        .map(unit -> {
                            RuleSet.UnitStats stats = rules.unit(unit.type());
                            return new Unit(
                                    unit.id(),
                                    unit.displayName() == null || unit.displayName().isBlank()
                                            ? unit.id()
                                            : unit.displayName(),
                                    unit.teamId(),
                                    unit.type(),
                                    new Position(unit.x(), unit.y()),
                                    stats.hp(),
                                    Direction.valueOf(unit.facing()),
                                    stats.size(),
                                    stats.size()
                            );
                        })
                        .toList(),
                headquarters,
                profile.resources().stream()
                        .map(resource -> new com.antcolonywars.core.ResourceNode(
                                resource.id(), resource.type(), new Position(resource.x(), resource.y()),
                                resource.amount()
                        ))
                        .toList(),
                profile.buildings().stream()
                        .map(building -> {
                            BuildingType type = BuildingType.valueOf(building.type());
                            return new Building(
                                    building.id(), building.teamId(), type,
                                    new Position(building.x(), building.y()), rules.building(type).hp()
                            );
                        })
                        .toList()
        ));
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }

    @Bean
    public MatchRegistry matchRegistry(
            MatchSetup setup,
            GameProfileProperties profile,
            MatchPersistence persistence,
            Clock clock
    ) {
        return new MatchRegistry(setup, profile.matchIds(), profile.limits(), clock, persistence);
    }

    @Bean
    public MatchCoordinator matchCoordinator(MatchSetup setup, MatchRegistry registry) {
        return registry.require(setup.matchId());
    }
}
