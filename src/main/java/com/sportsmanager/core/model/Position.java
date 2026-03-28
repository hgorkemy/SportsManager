package com.sportsmanager.core.model;

/**
 * Represents a playing position in a sport.
 *
 * TODO (Egemen): This interface is defined. No changes needed here.
 */
public interface Position {
    String getCode();
    String getDisplayName();
    String getPrimaryAttribute();
}
