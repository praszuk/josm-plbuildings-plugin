package org.openstreetmap.josm.plugins.plbuildings.actions;

import static org.openstreetmap.josm.plugins.plbuildings.utils.PostCheckUtils.findLifecyclePrefixBuildingTags;
import static org.openstreetmap.josm.plugins.plbuildings.utils.PostCheckUtils.findUncommonTags;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.stream.Collectors;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsPlugin;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy.FullImportStrategy;
import org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy.GeometryUpdateStrategy;
import org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy.ImportStrategy;
import org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy.TagsUpdateStrategy;
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.exceptions.ImportActionCanceledException;
import org.openstreetmap.josm.plugins.plbuildings.gui.LifecyclePrefixBuildingTagDialog;
import org.openstreetmap.josm.plugins.plbuildings.gui.UncommonTagDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

public class BuildingsImportAction extends JosmAction {
    static final String DESCRIPTION = tr(
        "Import the building at the location pointed to by the cursor and/or replace/update the selected one."
    );
    static final String TITLE = "PlBuildings: " + tr("Download building");
    static final BuildingsImportStats importStats = BuildingsImportStats.getInstance();

    public BuildingsImportAction() {
        super(
            TITLE,
            (ImageProvider) null,
            DESCRIPTION,
            Shortcut.registerShortcut(
                BuildingsPlugin.info.name + ":download:building",
                TITLE,
                KeyEvent.VK_1,
                Shortcut.CTRL_SHIFT
            ),
            false,
            BuildingsPlugin.info.name + ":buildings_import",
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
     * @return – selected way in give dataset or null
     */
    public static Way getSelectedWay(DataSet ds) {
        Collection<OsmPrimitive> selected = ds.getSelected()
            .stream()
            .filter(osmPrimitive -> osmPrimitive.getType() == OsmPrimitiveType.WAY)
            .collect(Collectors.toList());
        return selected.size() == 1 ? (Way) selected.toArray()[0] : null;
    }

    public static Bounds getUserFrameViewBounds() {
        MapFrame mapFrame = MainApplication.getMap();
        if (mapFrame == null) { // tests
            return null;

        }
        return mapFrame.mapView.getState().getViewArea().getLatLonBoundsBox();
    }

    public static boolean showDialogIfFoundUncommonTags(Way resultBuilding, BuildingsImportManager manager) {
        if (resultBuilding == null) {
            return false;
        }

        TagMap uncommon = findUncommonTags(resultBuilding);
        if (uncommon.isEmpty()) {
            return false;
        }

        Logging.debug("Found uncommon tags {0}", uncommon);
        manager.setStatus(ImportStatus.ACTION_REQUIRED, null);
        UncommonTagDialog.show(uncommon.getTags().toString().replace("[", "").replace("]", ""));
        return true;
    }

    public static void showDialogIfFoundLifecycleBuildingPrefixedTags(
        Way resultBuilding, BuildingsImportManager manager
    ) {
        if (resultBuilding == null) {
            return;
        }

        TagMap lifecyclePrefixBuildingTags = findLifecyclePrefixBuildingTags(resultBuilding);
        if (lifecyclePrefixBuildingTags.isEmpty()) {
            return;
        }

        Logging.debug("Found tags with life cycle prefixes {0}", lifecyclePrefixBuildingTags);
        manager.setStatus(ImportStatus.ACTION_REQUIRED, null);
        LifecyclePrefixBuildingTagDialog.show(
            lifecyclePrefixBuildingTags.getTags().toString().replace("[", "").replace("]", "")
        );
    }

    public static void performBuildingImport(BuildingsImportManager manager) {
        importStats.addTotalImportActionCounter(1);

        BuildingsImportData buildingsImportData = manager.getImportedData();
        if (buildingsImportData == null) {  // Some error at importing data
            return;
        }

        if (buildingsImportData.isOutOfUserFrameView(getUserFrameViewBounds())) {
            Logging.warn("Imported building data outside the user's view.");
            manager.setStatus(ImportStatus.IMPORT_ERROR, tr("Imported building data outside the user's view."));
            return;
        }

        Way importedBuilding = (Way) manager.getNearestImportedBuilding(
            buildingsImportData,
            manager.getCurrentProfile(),
            manager.getCursorLatLon()
        );
        if (importedBuilding == null) {
            Logging.info("Building not found.");
            manager.setStatus(ImportStatus.NO_DATA, tr("Building not found."));
            return;
        }
        // Add importedBuilding to DataSet to prevent DataIntegrityError (primitives without osm metadata)
        new DataSet().addPrimitiveRecursive(importedBuilding);

        ImportStrategy importStrategy;
        switch (BuildingsSettings.IMPORT_MODE.get()) {
            case FULL:
                importStrategy = new FullImportStrategy(manager, importStats, importedBuilding);
                break;
            case GEOMETRY:
                importStrategy = new GeometryUpdateStrategy(manager, importStats, importedBuilding);
                break;
            case TAGS:
                importStrategy = new TagsUpdateStrategy(manager, importStats, importedBuilding);
                break;
            default:
                Logging.error("Incorrect import mode: " + BuildingsSettings.IMPORT_MODE.get());
                manager.setStatus(ImportStatus.IMPORT_ERROR, tr("Incorrect import mode."));
                return;
        }

        try {
            Way resultBuilding = importStrategy.performImport();
            manager.setResultBuilding(resultBuilding);

            boolean hasUncommonTags = BuildingsSettings.UNCOMMON_TAGS_CHECK.get()
                && showDialogIfFoundUncommonTags(resultBuilding, manager);
            showDialogIfFoundLifecycleBuildingPrefixedTags(resultBuilding, manager);
            manager.setStatus(ImportStatus.DONE, null);
            manager.updateGuiTags(hasUncommonTags);
        } catch (ImportActionCanceledException exception) {
            Logging.info("{0} {1}", exception.getStatus(), exception.getMessage());
            manager.setStatus(exception.getStatus(), exception.getMessage());
        } finally {
            manager.getEditLayer().clearSelection();
            importStats.save();
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        DataSet currentDataSet = getLayerManager().getEditDataSet();

        // Get selection first – it must be got before starting downloading
        // to avoid changing incorrect building in future – which is possible if user importing so fast and
        // downloading takes longer then selecting next building to update
        Way selectedBuilding = getSelectedWay(currentDataSet);
        LatLon cursorLatLon = getCurrentCursorLocation();

        BuildingsImportManager buildingsImportManager = new BuildingsImportManager(
            currentDataSet,
            cursorLatLon,
            selectedBuilding
        );
        try {
            buildingsImportManager.validate();
        } catch (ImportActionCanceledException exception) {
            Logging.info("{0} {1}", exception.getStatus(), exception.getMessage());
            buildingsImportManager.setStatus(exception.getStatus(), exception.getMessage());
            return;
        }

        buildingsImportManager.run();
    }
}