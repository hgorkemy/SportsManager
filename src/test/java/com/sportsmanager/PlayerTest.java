package com.sportsmanager;

import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Player injury system.
 * Implemented by: Halil Görkem Yiğit
 */
class PlayerTest {

    private FootballPlayer player;

    @BeforeEach
    void setUp() {
        player = new FootballPlayer("Ali", "Yilmaz", 25, FootballPosition.FORWARD,
                80, 85, 70, 75, 40, 72);
    }

    @Test
    void playerIsNotInjuredInitially() {
        assertFalse(player.isInjured(), "New player should not be injured");
    }

    @Test
    void injureSetsDurationCorrectly() {
        player.injure(3);
        assertEquals(3, player.getInjuredGamesRemaining());
        assertTrue(player.isInjured());
    }

    @Test
    void recoverOneGameDecrementsCounter() {
        player.injure(3);
        player.recoverOneGame();
        assertEquals(2, player.getInjuredGamesRemaining());
    }

    @Test
    void playerFullyRecoveredAfterAllGames() {
        player.injure(3);
        player.recoverOneGame();
        player.recoverOneGame();
        player.recoverOneGame();
        assertFalse(player.isInjured(), "Player should be recovered after 3 recover calls");
        assertEquals(0, player.getInjuredGamesRemaining());
    }
}
