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

    private DownloaderConstants(){}
}
