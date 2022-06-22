package org.openstreetmap.josm.plugins.plbuildings.commands;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.UserCancelException;

import java.util.*;

import static org.openstreetmap.josm.tools.I18n.tr;


public class UpdateBuildingTagsCommand extends Command {

    static final String DESCRIPTION_TEXT = tr("Updated building tags");
    private final Way selectedBuilding;
    private final Way newBuilding;
    private SequenceCommand updateTagsCommand;

    public UpdateBuildingTagsCommand(Way selectedBuilding, Way newBuilding) {
        super(selectedBuilding.getDataSet());
        this.selectedBuilding = selectedBuilding;
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
        List<Command> commands;
        try {
            commands = prepareUpdateTagsCommands();
        } catch (UserCancelException exception) {
            Logging.debug(
                "No building tags (id: {0}) update, caused: Cancel conflict dialog by user",
                selectedBuilding.getId()
            );
            return false;
        }
        if (commands.isEmpty()){
            Logging.debug("Duplicated building geometry and tags! Canceling!");
            return false;
        }
        this.updateTagsCommand = new SequenceCommand(DESCRIPTION_TEXT, commands);
        UndoRedoHandler.getInstance().add(this.updateTagsCommand);

        return true;
    }

    /**
     * Wrapper function copied from Utilsplugin2 ReplaceGeometryUtils.getTagConflictResolutionCommands
     *
     * @return list of commands as updating tags
     * @throws UserCancelException if user close the window or reject possible tags conflict
     */
    private List<Command> prepareUpdateTagsCommands() throws UserCancelException {
        Collection<OsmPrimitive> primitives = Arrays.asList(selectedBuilding, newBuilding);

        return CombinePrimitiveResolverDialog.launchIfNecessary(
            TagCollection.unionOfAllPrimitives(primitives),
            primitives,
            Collections.singleton(selectedBuilding)
        );
    }
}
