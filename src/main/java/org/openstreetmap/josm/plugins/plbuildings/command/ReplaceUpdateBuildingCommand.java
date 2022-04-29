package org.openstreetmap.josm.plugins.plbuildings.command;

import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryCommand;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryException;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryUtils;

import java.util.ArrayList;
import java.util.Collection;

import static org.openstreetmap.josm.plugins.plbuildings.utils.TagConflictUtils.replaceNoConflictTags;
import static org.openstreetmap.josm.tools.I18n.tr;


public class ReplaceUpdateBuildingCommand extends Command {
    /**
     * Replace the old building geometry with the new one and update building tags
     */
    private final Way selectedBuilding;
    private final Way newBuilding;

    private ReplaceGeometryCommand replaceGeometryCommand;
    private ChangePropertyCommand replaceNoConflictTagsCommand;

    public ReplaceUpdateBuildingCommand(DataSet data, Way selectedBuilding, Way newBuilding) {
        super(data);
        this.selectedBuilding = selectedBuilding;
        this.newBuilding = newBuilding;
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
            primitives.add(selectedBuilding); // Nodes changed with replace geometry
            primitives.addAll(selectedBuilding.getNodes()); // Tags can be changed with ChangePropertyCommand
        }
        if (newBuilding != null){
            primitives.add(newBuilding); // way can be removed
            primitives.addAll(newBuilding.getNodes()); // some nodes can be moved to oldBuilding
        }
        return primitives;
    }

    @Override
    public void undoCommand() {
        replaceGeometryCommand.undoCommand();
        if (replaceNoConflictTagsCommand != null){
            replaceNoConflictTagsCommand.undoCommand();
        }
    }

    @Override
    public boolean executeCommand() {
        return replaceAndUpdate(selectedBuilding, newBuilding);
    }

    /**
     * Main execute command which handle 2 nested commands (tags updates and geometry building update)
     * @throws ReplaceGeometryException utilsplugin2 replaceGeometry exception
     * @throws IllegalArgumentException utilsplugin2 replaceGeometry exception
     */
    private boolean replaceAndUpdate(
            Way selectedBuilding,
            Way newBuilding
    ) throws ReplaceGeometryException, IllegalArgumentException {
        replaceNoConflictTagsCommand = replaceNoConflictTags(
                selectedBuilding,
                newBuilding,
                BuildingsSettings.REPLACE_BUILDING_TAG_NO_CONFLICT.get()
        );
        if (replaceNoConflictTagsCommand != null){
            replaceNoConflictTagsCommand.executeCommand();
        }

        try {
            replaceGeometryCommand = ReplaceGeometryUtils.buildReplaceWithNewCommand(
                    selectedBuilding,
                    newBuilding
            );
            replaceGeometryCommand.executeCommand();
        } catch (IllegalArgumentException msg) {
            // If user cancel conflict window do nothing
            if (replaceNoConflictTagsCommand != null){
                replaceNoConflictTagsCommand.undoCommand();
            }
            throw new IllegalArgumentException(msg.getMessage());
        } catch (ReplaceGeometryException msg) {
            // If selected building cannot be merged (e.g. connected ways/relation)
            if (replaceNoConflictTagsCommand != null){
                replaceNoConflictTagsCommand.undoCommand();
            }
            throw new ReplaceGeometryException(msg.getMessage());
        }
        return true;
    }

    @Override
    public String getDescriptionText() {
        return tr("Replace geometry and update tags");
    }
}
