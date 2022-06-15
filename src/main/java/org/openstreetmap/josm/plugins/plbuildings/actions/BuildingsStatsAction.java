package org.openstreetmap.josm.plugins.plbuildings.actions;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportStats;
import org.openstreetmap.josm.plugins.plbuildings.gui.BuildingsImportStatsPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;

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
        BuildingsImportStats buildingsStats = BuildingsImportStats.getInstance();
        LinkedHashMap<String, String> statsPanelData = new LinkedHashMap<>();
        statsPanelData.put(
            tr("Imported new building"),
            Integer.toString(buildingsStats.getImportCounter())
        );
        statsPanelData.put(
            tr("Imported with full replace"),
            Integer.toString(buildingsStats.getImportWithReplaceCounter())
        );
        statsPanelData.put(
            tr("Imported with tags update"),
            Integer.toString(buildingsStats.getImportWithTagsUpdateCounter())
        );
        statsPanelData.put(
            tr("Total import action"),
            Integer.toString(buildingsStats.getTotalImportActionCounter())
        );

        JOptionPane.showMessageDialog(
            null,
            new BuildingsImportStatsPanel(statsPanelData),
            TITLE,
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
