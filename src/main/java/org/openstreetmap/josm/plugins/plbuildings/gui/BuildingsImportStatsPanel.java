package org.openstreetmap.josm.plugins.plbuildings.gui;

import java.awt.GridLayout;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BuildingsImportStatsPanel extends JPanel {

    public BuildingsImportStatsPanel(HashMap<String, String> stats) {
        super();
        setLayout(new GridLayout(stats.size(), 2));
        stats.forEach((key, value) -> {
            add(new JLabel(key + ": "));
            add(new JLabel(value));
        });
    }
}
