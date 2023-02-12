package org.openstreetmap.josm.plugins.plbuildings.io;

import org.openstreetmap.josm.data.Version;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsPlugin;

public final class DownloaderConstants {
    public static final String USER_AGENT = String.format(
        "%s/%s %s",
        BuildingsPlugin.info.name,
        BuildingsPlugin.info.version,
        Version.getInstance().getFullAgentString()
    );

    public static final String API_NEAREST_BUILDING = "/buildings/nearest";
    public static final String API_DATA_SOURCES_PROFILES = "/profiles";

    private DownloaderConstants(){}
}
