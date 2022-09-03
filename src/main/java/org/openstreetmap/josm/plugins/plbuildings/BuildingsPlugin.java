package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsStatsAction;
import org.openstreetmap.josm.plugins.plbuildings.gui.BuildingsToggleDialog;

public class BuildingsPlugin extends Plugin {
    public static PluginInformation info;
    public static BuildingsToggleDialog buildingsToggleDialog;

    public BuildingsPlugin(PluginInformation info){
        super(info);
        BuildingsPlugin.info = info;
        MainMenu.add(MainApplication.getMenu().dataMenu, new BuildingsStatsAction());
        MainMenu.add(MainApplication.getMenu().selectionMenu, new BuildingsImportAction());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);
        if (newFrame != null){
            buildingsToggleDialog = new BuildingsToggleDialog();
            newFrame.addToggleDialog(buildingsToggleDialog);
        }
        else{
            buildingsToggleDialog = null;
        }
    }
}
