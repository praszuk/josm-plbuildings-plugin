package org.openstreetmap.josm.plugins.plbuildings.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsPlugin;
import org.openstreetmap.josm.plugins.plbuildings.gui.BuildingsImportStatsPanel;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

public class BuildingsStatsAction extends JosmAction {

    public static final String DESCRIPTION = tr("Show building import stats");
    public static final String TITLE = "PlBuildings: " + tr("Building import stats");

    public BuildingsStatsAction() {
        super(
            TITLE,
            (ImageProvider) null,
            DESCRIPTION,
            Shortcut.registerShortcut(
                BuildingsPlugin.info.name + ":import_stats",
                TITLE,
                KeyEvent.CHAR_UNDEFINED,
                Shortcut.NONE
            ),
            true,
            BuildingsPlugin.info.name + ":buildings_stats",
            false
        );
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        BuildingsImportStats buildingsStats = BuildingsImportStats.getInstance();
        LinkedHashMap<String, String> statsPanelData = new LinkedHashMap<>();
        statsPanelData.put(
            tr("New buildings"),
            Integer.toString(buildingsStats.getImportNewBuildingCounter())
        );
        statsPanelData.put(
            tr("Imports with building replace"),
            Integer.toString(buildingsStats.getImportWithReplaceCounter())
        );
        statsPanelData.put(
            tr("Imports with tags update"),
            Integer.toString(buildingsStats.getImportWithTagsUpdateCounter())
        );
        statsPanelData.put(
            tr("Imports with geometry update"),
            Integer.toString(buildingsStats.getImportWithGeometryUpdateCounter())
        );
        statsPanelData.put(
            tr("Total import actions"),
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
