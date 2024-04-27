package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsSettingsAction;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsStatsAction;
import org.openstreetmap.josm.plugins.plbuildings.controllers.ToggleDialogController;
import org.openstreetmap.josm.plugins.plbuildings.controllers.SettingsController;
import org.openstreetmap.josm.plugins.plbuildings.controllers.SettingsDataSourcesController;
import org.openstreetmap.josm.plugins.plbuildings.gui.BuildingsToggleDialog;
import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsDataSourcesPanel;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;

public class BuildingsPlugin extends Plugin {
    public static PluginInformation info;
    public static BuildingsToggleDialog buildingsToggleDialog;

    static ToggleDialogController toggleDialogController;
    private final BuildingsToggleDialog toggleDialog;

    public BuildingsPlugin(PluginInformation info){
        super(info);
        BuildingsPlugin.info = info;

        this.toggleDialog = new BuildingsToggleDialog();
        DataSourceConfig dataSourceConfig = DataSourceConfig.getInstance();

        if (BuildingsSettings.DATA_SOURCE_PROFILES_AUTO_REFRESH.get()){
            dataSourceConfig.refreshFromServer(true);
        }

        toggleDialogController = new ToggleDialogController(dataSourceConfig, toggleDialog);
        SettingsDataSourcesController settingsDataSourcesController = new SettingsDataSourcesController(dataSourceConfig, new SettingsDataSourcesPanel());

        SettingsController settingsController = new SettingsController(settingsDataSourcesController);

        MainMenu.add(MainApplication.getMenu().dataMenu, new BuildingsStatsAction());
        MainMenu.add(MainApplication.getMenu().dataMenu, new BuildingsSettingsAction(settingsController));
        MainMenu.add(MainApplication.getMenu().selectionMenu, new BuildingsImportAction());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);
        if (newFrame != null){
            newFrame.addToggleDialog(toggleDialog);
        }
    }
}
