package org.openstreetmap.josm.plugins.plbuildings.commands;

import static org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsWayValidator.isBuildingWayValid;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataIntegrityProblemException;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryCommand;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryException;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryUtils;
import org.openstreetmap.josm.tools.Logging;


public class ReplaceBuildingGeometryCommand extends Command implements CommandResultBuilding, CommandWithErrorReason {
    /**
     * Replace the old building geometry with the new one
     */
    private final Way selectedBuilding;
    private final CommandResultBuilding resultNewBuilding;
    private Way newBuilding;

    private ReplaceGeometryCommand replaceGeometryCommand;

    private String executeErrorReason;

    public ReplaceBuildingGeometryCommand(DataSet data, Way selectedBuilding, CommandResultBuilding resultNewBuilding) {
        super(data);
        this.selectedBuilding = selectedBuilding;
        this.resultNewBuilding = resultNewBuilding;
    }

    @Override
    public void fillModifiedData(
        Collection<OsmPrimitive> modified,
        Collection<OsmPrimitive> deleted,
        Collection<OsmPrimitive> added
    ) {
        updateGeometry(selectedBuilding, newBuilding);
        modified.add(selectedBuilding);
    }

    @Override
    public Collection<? extends OsmPrimitive> getParticipatingPrimitives() {
        Collection<OsmPrimitive> primitives = new ArrayList<>();
        if (selectedBuilding != null) {
            primitives.add(selectedBuilding);
            primitives.addAll(selectedBuilding.getNodes()); // Nodes changed with replace geometry
        }
        if (newBuilding != null) {
            primitives.add(newBuilding);
            primitives.addAll(newBuilding.getNodes()); // some nodes can be moved to oldBuilding
        }
        return primitives;
    }

    @Override
    public void undoCommand() {
        if (replaceGeometryCommand != null) {
            replaceGeometryCommand.undoCommand();
        }
    }

    @Override
    public boolean executeCommand() {
        this.newBuilding = resultNewBuilding.getResultBuilding();

        try {
            updateGeometry(selectedBuilding, newBuilding);
            Logging.debug("Updated geometry for the selected building: {0}", selectedBuilding);
            return true;
        } catch (Exception exception) {
            handleException(exception);
            return false;
        }
    }

    private void handleException(Exception exception) {
        // If user cancel conflict window
        if (exception instanceof IllegalArgumentException) {
            executeErrorReason = tr("Canceled merging buildings!");
            Logging.debug(
                "No building (id: {0}) update, caused: Cancel conflict dialog by user",
                selectedBuilding.getId()
            );
        }
        // If selected building cannot be merged (e.g. connected ways/relation)
        else if (exception instanceof ReplaceGeometryException) {
            executeErrorReason = tr(
                "Cannot merge buildings!"
                    + " Old building may be connected with some ways/relations"
                    + " or not whole area is downloaded!"
            );
            Logging.debug(
                "No building update (id: {0}), caused: Replacing Geometry from UtilPlugins2 error",
                selectedBuilding.getId()
            );
        }
        // If data integrity like nodes duplicated or first!=last has been somehow broken
        else if (exception instanceof DataIntegrityProblemException) {
            executeErrorReason = tr(
                "Cannot merge buildings! Building has been wrongly replaced and data has been broken!"
            );
            Logging.error(
                "No building update (id: {0}), caused: DataIntegrity with replacing error! Building: {1}",
                selectedBuilding.getId(),
                selectedBuilding
            );
        } else {
            executeErrorReason = tr("Cannot merge buildings! Unknown error!");
            Logging.error(
                "No building update (id: {0}), caused: Unknown error: {1}",
                selectedBuilding.getId(),
                exception.getMessage()
            );
        }
    }


    /**
     * Main execute command which handle geometry building update.
     *
     * @throws IllegalArgumentException      if user cancel conflict window
     * @throws NullPointerException          if user cancel conflict window
     * @throws ReplaceGeometryException      if selected building cannot be merged (e.g. connected ways/relation)
     * @throws DataIntegrityProblemException if something has been broken (low-level) at merging buildings
     */
    private void updateGeometry(Way selectedBuilding, Way newBuilding) throws IllegalArgumentException,
        NullPointerException, ReplaceGeometryException, DataIntegrityProblemException {
        replaceGeometryCommand = ReplaceGeometryUtils.buildReplaceWithNewCommand(
            selectedBuilding,
            newBuilding
        );
        replaceGeometryCommand.executeCommand();
        if (!isBuildingWayValid(selectedBuilding)) {
            throw new DataIntegrityProblemException("Wrongly merged building!");
        }
    }

    @Override
    public String getDescriptionText() {
        return tr("Replaced building geometry");
    }

    @Override
    public Way getResultBuilding() {
        return this.newBuilding;
    }

    @Override
    public String getErrorReason() {
        return executeErrorReason;
    }
}
