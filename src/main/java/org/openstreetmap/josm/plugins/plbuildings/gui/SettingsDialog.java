package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JFrame;
import org.openstreetmap.josm.gui.MainApplication;

public class SettingsDialog extends JFrame {
    final static int HEIGHT = 600;
    final static int WIDTH = 600;
    final static String TITLE = tr("PlBuildings Settings");

    public SettingsDialog(SettingsDataSourcesPanel settingsDataSourcesPanel) {
        super();
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(MainApplication.getMainFrame());
        setTitle(TITLE);

        add(settingsDataSourcesPanel);
        setVisible(true);
    }
}
