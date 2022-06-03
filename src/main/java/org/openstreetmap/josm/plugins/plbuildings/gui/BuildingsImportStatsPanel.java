package org.openstreetmap.josm.plugins.plbuildings.gui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class BuildingsImportStatsPanel extends JPanel {

    public BuildingsImportStatsPanel(HashMap<String, String> stats){
        super();
        setLayout(new GridLayout(stats.size(), 2));
        stats.forEach((key, value) -> {
            add(new JLabel(key + ": "));
            add(new JLabel(value));
        });
    }
}
