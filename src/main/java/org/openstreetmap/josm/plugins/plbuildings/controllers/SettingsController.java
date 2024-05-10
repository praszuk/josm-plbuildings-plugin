package org.openstreetmap.josm.plugins.plbuildings.controllers;

import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsDialog;

public class SettingsController {

    private final SettingsDataSourcesController settingsDataSourcesController;

    public SettingsController(SettingsDataSourcesController settingsDataSourcesController) {
        this.settingsDataSourcesController = settingsDataSourcesController;
    }

    public void initGui() {
        new SettingsDialog(settingsDataSourcesController.getSettingsDataSourcesPanelView());
    }
}
