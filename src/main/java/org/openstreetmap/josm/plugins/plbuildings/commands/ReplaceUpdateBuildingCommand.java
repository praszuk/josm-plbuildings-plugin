package org.openstreetmap.josm.plugins.plbuildings.commands;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataIntegrityProblemException;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryCommand;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryException;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryUtils;
import org.openstreetmap.josm.tools.Logging;

import java.util.ArrayList;
import java.util.Collection;

import static org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsWayValidator.isBuildingWayValid;
import static org.openstreetmap.josm.tools.I18n.tr;


public class ReplaceUpdateBuildingCommand extends Command implements CommandResultBuilding {
    /**
     * Replace the old building geometry with the new one and update building tags
     */
    private final Way selectedBuilding;
    private final CommandResultBuilding resultNewBuilding;
    private Way newBuilding;

    private ReplaceGeometryCommand replaceGeometryCommand;

    private Exception cancelException;

    public ReplaceUpdateBuildingCommand(DataSet data, Way selectedBuilding, CommandResultBuilding resultNewBuilding) {
        super(data);
        this.selectedBuilding = selectedBuilding;
        this.resultNewBuilding = resultNewBuilding;
        this.cancelException = null;
    }

    public Exception getCancelException() {
        return cancelException;
    }

    @Override
    public void fillModifiedData(
            Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted,
            Collection<OsmPrimitive> added
    ) {
        replaceAndUpdate(selectedBuilding, newBuilding);
        modified.add(selectedBuilding);
    }

    @Override
    public Collection<? extends OsmPrimitive> getParticipatingPrimitives() {
        // I am not sure if I implemented it correctly.
        Collection<OsmPrimitive> primitives = new ArrayList<>();
        if (selectedBuilding != null){
            primitives.add(selectedBuilding);
            primitives.addAll(selectedBuilding.getNodes()); // Nodes changed with replace geometry
        }
        if (newBuilding != null){
            primitives.add(newBuilding);
            primitives.addAll(newBuilding.getNodes()); // some nodes can be moved to oldBuilding
        }
        return primitives;
    }

    @Override
    public void undoCommand() {
        if (replaceGeometryCommand != null){
            replaceGeometryCommand.undoCommand();
        }
        this.cancelException = null;
    }

    @Override
    public boolean executeCommand() {
        this.cancelException = null;
        this.newBuilding = resultNewBuilding.getResultBuilding();
        return replaceAndUpdate(selectedBuilding, newBuilding);
    }

    /**
     * Main execute command which handle 2 nested commands (tags updates and geometry building update)
     * @return false if any exception else true
     */
    private boolean replaceAndUpdate(Way selectedBuilding, Way newBuilding){
        try {
            replaceGeometryCommand = ReplaceGeometryUtils.buildReplaceWithNewCommand(
                selectedBuilding,
                newBuilding
            );
            replaceGeometryCommand.executeCommand();
            if (!isBuildingWayValid(selectedBuilding)){
                throw new DataIntegrityProblemException("Wrongly merged building!");
            }

        } catch (IllegalArgumentException | NullPointerException msg) {
            // If user cancel conflict window do nothing
            this.cancelException = new IllegalArgumentException(msg.getMessage());
            return false;
        } catch (ReplaceGeometryException msg) {
            // If selected building cannot be merged (e.g. connected ways/relation)
            this.cancelException = new ReplaceGeometryException(msg.getMessage());
            return false;
        } catch(DataIntegrityProblemException msg){
            // If something has been broken (low-level) at merging buildings
            this.cancelException = new DataIntegrityProblemException(msg.getMessage());
            return false;
        }
        Logging.debug("Updated tags for the selected building: {0}", selectedBuilding);
        return true;
    }

    @Override
    public String getDescriptionText() {
        return tr("Replace geometry and update tags");
    }

    @Override
    public Way getResultBuilding() {
        return this.newBuilding;
    }
}
