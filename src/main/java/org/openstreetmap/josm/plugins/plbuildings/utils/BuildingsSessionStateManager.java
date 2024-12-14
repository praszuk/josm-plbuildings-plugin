package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy;

public class BuildingsSessionStateManager {
    private static CombineNearestOneDsStrategy oneDsConfirmationSessionStrategy = null;

    public static CombineNearestOneDsStrategy getOneDsConfirmationSessionStrategy() {
        return oneDsConfirmationSessionStrategy;
    }

    public static void setOneDsConfirmationSessionStrategy(CombineNearestOneDsStrategy strategy) {
        oneDsConfirmationSessionStrategy = strategy;
    }
}
