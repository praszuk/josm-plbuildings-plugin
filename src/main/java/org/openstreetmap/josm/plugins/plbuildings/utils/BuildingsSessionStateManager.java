package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOverlappingStrategy;

public class BuildingsSessionStateManager {
    private static CombineNearestOneDsStrategy oneDsConfirmationSessionStrategy = null;
    private static CombineNearestOverlappingStrategy overlappingConfirmationSessionStrategy = null;

    public static CombineNearestOneDsStrategy getOneDsConfirmationSessionStrategy() {
        return oneDsConfirmationSessionStrategy;
    }

    public static void setOneDsConfirmationSessionStrategy(CombineNearestOneDsStrategy strategy) {
        oneDsConfirmationSessionStrategy = strategy;
    }

    public static CombineNearestOverlappingStrategy getOverlappingConfirmationSessionStrategy() {
        return overlappingConfirmationSessionStrategy;
    }

    public static void setOverlappingConfirmationSessionStrategy(CombineNearestOverlappingStrategy strategy) {
        overlappingConfirmationSessionStrategy = strategy;
    }
}
