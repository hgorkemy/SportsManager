package com.sportsmanager;

import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.football.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    private FootballTeam team;

    @BeforeEach
    void setUp() {
        team = new FootballTeam("Test FC", null);
    }

    /** addPlayer() increases the squad size by exactly 1. */
    @Test
    void addPlayer_increaseSquadSize() {
        int before = team.getSquad().size();
        team.addPlayer(new FootballPlayer("John", "Doe", 25, FootballPosition.MIDFIELDER,
                70, 70, 70, 70, 70, 70));
        assertEquals(before + 1, team.getSquad().size());
    }

    /** setLineup() with 11 players including 1 GK succeeds (no exception, lineup size == 11). */
    @Test
    void setLineup_validLineup_succeeds() {
        List<Player> lineup = buildLineup(true);   // includes GK
        assertDoesNotThrow(() -> team.setLineup(lineup));
        assertEquals(11, team.getLineup().size());
    }

    /** setLineup() with 11 players but NO goalkeeper throws IllegalArgumentException. */
    @Test
    void setLineup_missingGK_throwsException() {
        List<Player> lineup = buildLineup(false);  // no GK
        assertThrows(IllegalArgumentException.class, () -> team.setLineup(lineup));
        assertTrue(team.getLineup().isEmpty(), "Lineup should be cleared after failed validation");
    }

    /** FootballFactory.createLeague() returns a non-null, non-empty league. */
    @Test
    void footbalFactory_createLeague_returnsNonEmptyLeague() {
        FootballFactory factory = new FootballFactory();
        List<Team> teams = factory.generateTeams(20);
        League league = factory.createLeague(teams);
        assertNotNull(league);
        assertFalse(league.getTeams().isEmpty());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds an 11-player list: 1 GK + 4 DEF + 4 MID + 2 FWD.
     * If withGK is false, the GK slot is replaced with an extra DEF.
     */
    private List<Player> buildLineup(boolean withGK) {
        List<Player> players = new ArrayList<>();

        FootballPosition firstPos = withGK ? FootballPosition.GOALKEEPER : FootballPosition.DEFENDER;
        addToTeamAndList(players, firstPos, 1);
        addToTeamAndList(players, FootballPosition.DEFENDER,   4);
        addToTeamAndList(players, FootballPosition.MIDFIELDER, 4);
        addToTeamAndList(players, FootballPosition.FORWARD,    2);

        return players; // always 11
    }

    private void addToTeamAndList(List<Player> list, FootballPosition pos, int count) {
        for (int i = 0; i < count; i++) {
            FootballPlayer p = new FootballPlayer(
                    pos.getCode() + i, "Player", 22, pos,
                    70, 65, 68, 68, 70, 68);
            team.addPlayer(p);
            list.add(p);
        }
    }
}
