package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsSettingsAction;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsStatsAction;
import org.openstreetmap.josm.plugins.plbuildings.controllers.SettingsController;
import org.openstreetmap.josm.plugins.plbuildings.controllers.SettingsDataSourcesController;
import org.openstreetmap.josm.plugins.plbuildings.controllers.ToggleDialogController;
import org.openstreetmap.josm.plugins.plbuildings.gui.BuildingsToggleDialog;
import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsDataSourcesPanel;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;

public class BuildingsPlugin extends Plugin {
    public static PluginInformation info;
    protected static ToggleDialogController toggleDialogController;

    public BuildingsPlugin(PluginInformation info) {
        super(info);
        BuildingsPlugin.info = info;

        DataSourceConfig dataSourceConfig = DataSourceConfig.getInstance();
        if (BuildingsSettings.DATA_SOURCE_PROFILES_AUTO_REFRESH.get()) {
            dataSourceConfig.refreshFromServer(true);
        }

        SettingsDataSourcesController settingsDataSourcesController =
            new SettingsDataSourcesController(dataSourceConfig, new SettingsDataSourcesPanel());
        SettingsController settingsController = new SettingsController(settingsDataSourcesController);

        MainMenu.add(MainApplication.getMenu().dataMenu, new BuildingsStatsAction());
        MainMenu.add(MainApplication.getMenu().dataMenu,
            new BuildingsSettingsAction(settingsController));
        MainMenu.add(MainApplication.getMenu().selectionMenu, new BuildingsImportAction());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);
        if (newFrame != null) {
            BuildingsToggleDialog toggleDialog = new BuildingsToggleDialog();
            toggleDialogController = new ToggleDialogController(DataSourceConfig.getInstance(), toggleDialog);
            newFrame.addToggleDialog(toggleDialog);
        } else {
            toggleDialogController = null;
        }
    }
}
