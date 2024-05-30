package org.openstreetmap.josm.plugins.plbuildings.controllers;

import java.awt.Component;

public interface SettingsTabController {
    String getTabTitle();

    Component getTabView();
}
