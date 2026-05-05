package com.sportsmanager.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads team/player name data from a JSON resource file.
 *
 * Expected JSON shape:
 * {
 *   "teams":      ["Team A", ...],
 *   "firstNames": ["Alice", ...],
 *   "lastNames":  ["Smith", ...]
 * }
 *
 * Implemented by: Yavuz Mete Afsar
 */
public class TeamDataLoader {

    private static final Gson GSON = new Gson();

    /**
     * Reads a named String array from a classpath resource.
     *
     * @param resourcePath absolute classpath path, e.g. "/data/handball_teams.json"
     * @param field        JSON key whose value is a String array
     * @return list of strings for that field
     * @throws RuntimeException if the resource is missing or the JSON is malformed
     */
    public static List<String> loadField(String resourcePath, String field) {
        try (InputStream is = TeamDataLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Resource not found on classpath: " + resourcePath);
            }
            JsonObject root = GSON.fromJson(
                    new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
            JsonArray array = root.getAsJsonArray(field);
            if (array == null) {
                throw new RuntimeException("Field '" + field + "' not found in " + resourcePath);
            }
            List<String> result = new ArrayList<>(array.size());
            array.forEach(e -> result.add(e.getAsString()));
            return result;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse " + resourcePath, e);
        }
    }
}
