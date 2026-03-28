package com.sportsmanager;

import com.sportsmanager.core.model.GameSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameSession singleton.
 * Implemented by: Halil Görkem Yiğit
 */
class GameSessionTest {

    @BeforeEach
    void setUp() {
        GameSession.getInstance().reset();
    }

    @Test
    void singletonReturnsSameInstance() {
        GameSession a = GameSession.getInstance();
        GameSession b = GameSession.getInstance();
        assertSame(a, b, "getInstance() must always return the same object");
    }

    @Test
    void isNotActiveBeforeStartNewGame() {
        assertFalse(GameSession.getInstance().isActive(),
                "Session should not be active before startNewGame()");
    }

    @Test
    void resetClearsState() {
        GameSession session = GameSession.getInstance();
        session.reset();
        assertFalse(session.isActive());
        assertNull(session.getSport());
        assertNull(session.getLeague());
        assertNull(session.getUserTeam());
    }
}
