package org.openstreetmap.josm.plugins.plbuildings.actions;

import static org.openstreetmap.josm.plugins.plbuildings.utils.PostCheckUtils.findUncommonTags;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.stream.Collectors;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsPlugin;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy.FullImportStrategy;
import org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy.ImportStrategy;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.gui.UncommonTagDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

public class BuildingsImportAction extends JosmAction {
    static final String DESCRIPTION =
        tr("Import building at cursor position or replace/update selected.");
    static final String TITLE = tr("Download building");

    public BuildingsImportAction() {
        super(
            TITLE,
            (ImageProvider) null,
            DESCRIPTION,
            Shortcut.registerShortcut(
                "download:building",
                TITLE,
                KeyEvent.VK_1,
                Shortcut.CTRL_SHIFT
            ),
            false,
            String.format("%s:buildings_import", BuildingsPlugin.info.name),
            false
        );
    }

    public static LatLon getCurrentCursorLocation() {
        try {
            return MainApplication.getMap().mapView.getLatLon(
                MainApplication.getMap().mapView.getMousePosition().getX(),
                MainApplication.getMap().mapView.getMousePosition().getY()
            );
        } catch (NullPointerException exception) {
            return null;
        }
    }

    /**
     * @return – selected building in give dataset or null
     */
    public static Way getSelectedBuilding(DataSet ds) {
        Collection<OsmPrimitive> selected = ds.getSelected()
            .stream()
            .filter(osmPrimitive -> osmPrimitive.getType() == OsmPrimitiveType.WAY)
            .collect(Collectors.toList());
        return selected.size() == 1 ? (Way) selected.toArray()[0] : null;
    }

    /**
     * Flow:
     * Validate imported datasets with preprocessing to get prepared import data
     * (matching multiple datasets).
     * Check if it's unique (no geometry duplicate):
     * -- duplicate:
     * ---- check if 1 building is selected:
     * ------ selected -> try to update tags
     * ------ not selected -> end
     * -- not duplicate:
     * ---- check if 1 building is selected:
     * ------ selected -> try to replace geometry and update tags
     * ------ not selected -> just import new building
     */
    public static void performBuildingImport(BuildingsImportManager manager) {
        // TODO refactor
        final BuildingsImportStats importStats = new BuildingsImportStats();
        importStats.addTotalImportActionCounter(1);

        BuildingsImportData buildingsImportData = manager.getImportedData();
        if (buildingsImportData == null) {  // Some error at importing data
            return;
        }

        Way importedBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            buildingsImportData,
            manager.getCurrentProfile(),
            manager.getCursorLatLon()
        );
        if (importedBuilding == null) {
            Logging.info("Building not found.");
            manager.setStatus(ImportStatus.NO_DATA, tr("Building not found."));
            return;
        }
        // Add importedBuilding to DataSet – it's needed to avoid DataIntegrityError (primitives without osm metadata)
        DataSet importDataSet = new DataSet();
        importDataSet.addPrimitiveRecursive(importedBuilding);

        ImportStrategy importStrategy = new FullImportStrategy(manager, importStats, importedBuilding);
        // ImportStrategy importStrategy = new GeometryUpdateStrategy(manager, importStats, importedBuilding); TODO
        // ImportStrategy importStrategy = new TagsUpdateStrategy(manager, importStats, importedBuilding); TODO
        Way resultBuilding = importStrategy.performImport();
        manager.setResultBuilding(resultBuilding);

        boolean hasUncommonTags = false;
        if (resultBuilding != null && BuildingsSettings.UNCOMMON_TAGS_CHECK.get()) {
            TagMap uncommon = findUncommonTags(resultBuilding);
            if (!uncommon.isEmpty()) {
                Logging.debug("Found uncommon tags {0}", uncommon);
                manager.setStatus(ImportStatus.ACTION_REQUIRED, null);
                UncommonTagDialog.show(
                    uncommon.getTags().toString().replace("[", "").replace("]", "")
                );
            }
        }
        manager.setStatus(ImportStatus.DONE, null);
        manager.updateGuiTags(hasUncommonTags);
        manager.getEditLayer().clearSelection();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        DataSet currentDataSet = getLayerManager().getEditDataSet();

        // Get selection first – it must be got before starting downloading
        // to avoid changing incorrect building in future – which is possible if user importing so fast and
        // downloading takes longer then selecting next building to update
        Way selectedBuilding = getSelectedBuilding(currentDataSet);
        LatLon cursorLatLon = getCurrentCursorLocation();

        BuildingsImportManager buildingsImportManager = new BuildingsImportManager(
            currentDataSet,
            cursorLatLon,
            selectedBuilding
        );
        if (buildingsImportManager.getCurrentProfile() == null) {
            Logging.info("BuildingsImportAction canceled! No DataSourceProfile selected!");
            return;
        }
        buildingsImportManager.run();
    }
}