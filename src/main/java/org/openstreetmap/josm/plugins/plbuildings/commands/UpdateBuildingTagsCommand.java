package org.openstreetmap.josm.plugins.plbuildings.commands;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.UserCancelException;

import java.util.*;

import static org.openstreetmap.josm.tools.I18n.tr;


public class UpdateBuildingTagsCommand extends Command implements CommandResultBuilding{

    static final String DESCRIPTION_TEXT = tr("Updated building tags");
    private final CommandResultBuilding resultSelectedBuilding;
    private final Way newBuilding;
    private Way selectedBuilding;
    private SequenceCommand updateTagsCommand;

    public UpdateBuildingTagsCommand(DataSet dataSet, CommandResultBuilding resultSelectedBuilding, Way newBuilding) {
        super(dataSet);
        this.resultSelectedBuilding = resultSelectedBuilding;
        this.newBuilding = newBuilding;
        this.updateTagsCommand = null;
    }

    @Override
    public void fillModifiedData(
        Collection<OsmPrimitive> modified,
        Collection<OsmPrimitive> deleted,
        Collection<OsmPrimitive> added
    ) {
        modified.add(selectedBuilding);
    }

    @Override
    public Collection<? extends OsmPrimitive> getParticipatingPrimitives() {
        // I am not sure if I implemented it correctly.
        Collection<OsmPrimitive> primitives = new ArrayList<>();
        if (selectedBuilding != null){
            primitives.add(selectedBuilding); // Tags change
        }
        return primitives;
    }

    @Override
    public void undoCommand() {
        if (updateTagsCommand != null){
            updateTagsCommand.undoCommand();
        }
    }

    @Override
    public String getDescriptionText() {
        return DESCRIPTION_TEXT;
    }

    @Override
    public boolean executeCommand() {
        if (this.updateTagsCommand == null) {
            this.selectedBuilding = resultSelectedBuilding.getResultBuilding();
            List<Command> commands;
            try {
                commands = prepareUpdateTagsCommands(selectedBuilding, newBuilding);
            } catch (UserCancelException exception) {
                Logging.debug(
                    "No building tags (id: {0}) update, caused: Cancel conflict dialog by user",
                    selectedBuilding.getId()
                );
                return false;
            }
            if (commands.isEmpty()) {
                Logging.debug("No tags difference! Canceling!");
                return true;
            }
            this.updateTagsCommand = new SequenceCommand(DESCRIPTION_TEXT, commands);
        }
        this.updateTagsCommand.executeCommand();

        return true;
    }

    /**
     * Wrapper function copied from Utilsplugin2 ReplaceGeometryUtils.getTagConflictResolutionCommands
     *
     * @return list of commands as updating tags
     * @throws UserCancelException if user close the window or reject possible tags conflict
     */
    protected List<Command> prepareUpdateTagsCommands(
        Way selectedBuilding,
        Way newBuilding
    ) throws UserCancelException {
        Collection<OsmPrimitive> primitives = Arrays.asList(selectedBuilding, newBuilding);

        return CombinePrimitiveResolverDialog.launchIfNecessary(
            TagCollection.unionOfAllPrimitives(primitives),
            primitives,
            Collections.singleton(selectedBuilding)
        );
    }

    @Override
    public Way getResultBuilding() {
        return this.selectedBuilding;
    }
}
