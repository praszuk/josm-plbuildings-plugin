package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import org.openstreetmap.josm.gui.MainApplication;

public class SettingsDialog extends JFrame {
    static final int HEIGHT = 600;
    static final int WIDTH = 600;
    static final String TITLE = tr("PlBuildings settings");

    private final JTabbedPane tabbedPane;

    public SettingsDialog() {
        super();
        this.tabbedPane = new JTabbedPane();
        add(tabbedPane);

        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(MainApplication.getMainFrame());
        setTitle(TITLE);
        setVisible(true);
    }

    public void addTab(String title, Component tab) {
        tabbedPane.addTab(title, tab);
    }
}
