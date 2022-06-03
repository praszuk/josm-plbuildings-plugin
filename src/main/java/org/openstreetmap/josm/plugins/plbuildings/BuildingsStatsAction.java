package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.actions.JosmAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import static org.openstreetmap.josm.tools.I18n.tr;

public class BuildingsStatsAction extends JosmAction {

    public static final String DESCRIPTION = tr("Show buildings import stats");
    public static final String TITLE = tr("Buildings import stats");
    public BuildingsStatsAction(){
        super(
            TITLE,
            null,
            DESCRIPTION,
            null,
            true
        );
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        HashMap<String, Object> stats = BuildingsImportStats.getInstance().getStats();
        JPanel panel = new JPanel(new GridLayout(stats.size(), 2));
        stats.forEach((key, value) -> {
            panel.add(new JLabel(key + ": "));
            panel.add(new JLabel(value.toString()));
        });

        JOptionPane.showMessageDialog(null, panel, TITLE, JOptionPane.INFORMATION_MESSAGE);
    }
}
