package com.sportsmanager.util;

import com.sportsmanager.core.model.Team;
import javafx.scene.image.Image;

import java.util.Map;

/**
 * Central utility for loading team logo images.
 *
 * Usage (any controller):
 *   Image img = TeamLogoHelper.load(team, 48);
 *
 * Explicit name→file mapping handles special cases like "FC Barcelona" → "barcelona".
 * Falls back to team.getLogoPath() for teams not in the map.
 *
 * Implemented by: Yavuz Mete Afsar
 */
public class TeamLogoHelper {

    private static final String BASE = "/images/logos/";

    private static final Map<String, String> NAME_TO_FILE = Map.ofEntries(
        Map.entry("Galatasaray",       "galatasaray"),
        Map.entry("Fenerbahçe",        "fenerbahce"),
        Map.entry("Beşiktaş",          "besiktas"),
        Map.entry("Trabzonspor",       "trabzonspor"),
        Map.entry("Göztepe",           "goztepe"),
        Map.entry("Bayern Munich",     "bayern_munich"),
        Map.entry("Borussia Dortmund", "borussia_dortmund"),
        Map.entry("Bayer Leverkusen",  "bayer_leverkusen"),
        Map.entry("FC Barcelona",      "barcelona"),
        Map.entry("Real Madrid",       "real_madrid"),
        Map.entry("Atletico Madrid",   "atletico_madrid"),
        Map.entry("Juventus",          "juventus"),
        Map.entry("Inter Milan",       "inter_milan"),
        Map.entry("AC Milan",          "ac_milan"),
        Map.entry("Napoli",            "napoli"),
        Map.entry("Manchester United", "manchester_united"),
        Map.entry("Manchester City",   "manchester_city"),
        Map.entry("Liverpool",         "liverpool"),
        Map.entry("Arsenal",           "arsenal"),
        Map.entry("PSG",               "psg")
    );

    /**
     * Loads the logo for a team at the given size (square, aspect-ratio preserved).
     * Returns null if no logo is found.
     */
    public static Image load(Team team, double size) {
        if (team == null) return null;

        // 1. Explicit name lookup (most reliable)
        String file = NAME_TO_FILE.get(team.getName());
        if (file != null) {
            var url = TeamLogoHelper.class.getResource(BASE + file + ".png");
            if (url != null) return new Image(url.toExternalForm(), size, size, true, true);
        }

        // 2. Fallback: use logoPath stored on the team
        String logoPath = team.getLogoPath();
        if (logoPath != null && !logoPath.isBlank()) {
            var url = TeamLogoHelper.class.getResource(logoPath);
            if (url != null) return new Image(url.toExternalForm(), size, size, true, true);
        }

        return null;
    }
}
