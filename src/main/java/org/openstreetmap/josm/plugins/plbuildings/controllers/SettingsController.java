package org.openstreetmap.josm.plugins.plbuildings.controllers;

import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsDialog;

public class SettingsController {

    private final SettingsDataSourcesController settingsDataSourcesController;
    private final SettingsNotificationsController settingsNotificationsController;

    public SettingsController(SettingsDataSourcesController settingsDataSourcesController,
                              SettingsNotificationsController settingsNotificationsController) {
        this.settingsDataSourcesController = settingsDataSourcesController;
        this.settingsNotificationsController = settingsNotificationsController;
    }

    public void initGui() {
        new SettingsDialog(settingsDataSourcesController.getSettingsDataSourcesPanelView(),
            settingsNotificationsController.getSettingsNotificationsPanelView());
    }
}
