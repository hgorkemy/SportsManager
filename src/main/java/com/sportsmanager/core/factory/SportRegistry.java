package com.sportsmanager.core.factory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for available sport factories.
 * Keeps UI decoupled from sport-specific implementations.
 */
public final class SportRegistry {

    private static final Map<String, SportFactory> FACTORIES = new LinkedHashMap<>();

    private SportRegistry() {
    }

    public static void register(String sportName, SportFactory factory) {
        FACTORIES.put(sportName, factory);
    }

    public static SportFactory getFactory(String sportName) {
        return FACTORIES.get(sportName);
    }

    public static List<String> getRegisteredSports() {
        return new ArrayList<>(FACTORIES.keySet());
    }
}
