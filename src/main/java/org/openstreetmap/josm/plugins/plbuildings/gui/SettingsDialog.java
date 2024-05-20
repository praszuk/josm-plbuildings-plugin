package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import org.openstreetmap.josm.gui.MainApplication;

public class SettingsDialog extends JFrame {
    static final int HEIGHT = 600;
    static final int WIDTH = 600;
    static final String TITLE = tr("PlBuildings Settings");

    public SettingsDialog(SettingsDataSourcesPanel settingsDataSourcesPanel,
                          SettingsNotificationsPanel settingsNotificationsPanel) {
        super();
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(MainApplication.getMainFrame());
        setTitle(TITLE);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(tr("Data sources"), settingsDataSourcesPanel);
        tabbedPane.addTab(tr("Notifications"), settingsNotificationsPanel);
        add(tabbedPane);

        setVisible(true);
    }
}
