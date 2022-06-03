package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class BuildingsPlugin extends Plugin {
    public static PluginInformation info;
    public BuildingsPlugin(PluginInformation info){
        super(info);
        BuildingsPlugin.info = info;
        MainMenu.add(MainApplication.getMenu().dataMenu, new BuildingsStatsAction());
        MainMenu.add(MainApplication.getMenu().selectionMenu, new BuildingsAction());
    }
}
