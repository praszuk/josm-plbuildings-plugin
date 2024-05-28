package org.openstreetmap.josm.plugins.plbuildings.controllers;

import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsDialog;

public class SettingsController {

    private final SettingsDataSourcesController settingsDataSourcesController;
    private final SettingsNotificationsController settingsNotificationsController;
    private final SettingsUncommonTagsController settingsUncommonTagsController;

    public SettingsController(SettingsDataSourcesController settingsDataSourcesController,
                              SettingsNotificationsController settingsNotificationsController,
                              SettingsUncommonTagsController settingsUncommonTagsController) {
        this.settingsDataSourcesController = settingsDataSourcesController;
        this.settingsNotificationsController = settingsNotificationsController;
        this.settingsUncommonTagsController = settingsUncommonTagsController;
    }

    public void initGui() {
        new SettingsDialog(
            settingsDataSourcesController.getSettingsDataSourcesPanelView(),
            settingsNotificationsController.getSettingsNotificationsPanelView(),
            settingsUncommonTagsController.getSettingsUncommonTagsPanelView()
        );
    }
}
