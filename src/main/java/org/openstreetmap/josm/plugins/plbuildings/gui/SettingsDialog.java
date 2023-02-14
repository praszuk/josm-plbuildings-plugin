package org.openstreetmap.josm.plugins.plbuildings.gui;
import org.openstreetmap.josm.gui.MainApplication;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.tr;

public class SettingsDialog extends JFrame {
    final static int HEIGHT = 600;
    final static int WIDTH = 600;
    final static String TITLE = tr("PlBuildings Settings");

    public SettingsDialog(){
        super();
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(MainApplication.getMainFrame());
        setTitle(TITLE);

        add(new SettingsDataSourcesPanel());
        setVisible(true);
    }
}
