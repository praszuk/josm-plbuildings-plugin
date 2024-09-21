package org.openstreetmap.josm.plugins.plbuildings.controllers;

import java.util.ArrayList;
import java.util.List;
import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsDialog;

public class SettingsController {

    private final List<SettingsTabController> settingsTabControllers;

    public SettingsController(List<SettingsTabController> controllers) {
        this.settingsTabControllers = new ArrayList<>(controllers);
    }

    public void initGui() {
        SettingsDialog settingsDialog = new SettingsDialog();
        for (SettingsTabController tab : settingsTabControllers) {
            settingsDialog.addTab(tab.getTabTitle(), tab.getTabView());
        }
    }
}
