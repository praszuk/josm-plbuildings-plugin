package org.openstreetmap.josm.plugins.plbuildings.commands;

import static org.openstreetmap.josm.plugins.plbuildings.utils.TagConflictUtils.resolveTagConflictsDefault;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.UserCancelException;


public class UpdateBuildingTagsCommand extends Command implements CommandResultBuilding, CommandWithErrorReason {

    static final String DESCRIPTION_TEXT = tr("Updated building tags");
    private final CommandResultBuilding resultSelectedBuilding;
    private final Way newBuilding;
    private Way selectedBuilding;
    private SequenceCommand updateTagsCommand;

    private String executeErrorReason;

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
        Collection<OsmPrimitive> primitives = new ArrayList<>();
        if (selectedBuilding != null) {
            primitives.add(selectedBuilding);  // Tags change
        }
        return primitives;
    }

    @Override
    public void undoCommand() {
        if (updateTagsCommand != null) {
            updateTagsCommand.undoCommand();
        }
    }

    @Override
    public String getDescriptionText() {
        return DESCRIPTION_TEXT;
    }


    private Command removeSourceGeoportal() {
        if (!BuildingsSettings.AUTOREMOVE_SOURCE_GEOPORTAL_GOV_PL.get()) {
            return null;
        }
        if (!selectedBuilding.hasTag("source") || !selectedBuilding.get("source").contains("geoportal.gov.pl")) {
            return null;
        }
        return new ChangePropertyCommand(selectedBuilding, "source", null);
    }

    @Override
    public boolean executeCommand() {
        if (this.updateTagsCommand == null) {
            this.selectedBuilding = resultSelectedBuilding.getResultBuilding();
            List<Command> commands;
            try {
                commands = new ArrayList<>(prepareUpdateTagsCommands(selectedBuilding, newBuilding));
            } catch (UserCancelException exception) {
                Logging.debug(
                    "No building tags (id: {0}) update, caused: Cancel conflict dialog by user",
                    selectedBuilding.getId()
                );
                executeErrorReason = tr("Conflict tag dialog canceled by user");
                return false;
            }

            Command removeSourceGeoportal = removeSourceGeoportal();
            if (removeSourceGeoportal != null) {
                commands.add(removeSourceGeoportal);
            }

            if (commands.isEmpty()) {
                Logging.debug("No tags difference! Canceling!");
                return true;
            }
            this.updateTagsCommand = new SequenceCommand(DESCRIPTION_TEXT, commands);
        }
        this.updateTagsCommand.executeCommand();
        Logging.debug("Updated tags for the building: {0}", selectedBuilding);
        return true;
    }

    /**
     * Prepare update tags command using CombinePrimitiveResolverDialog before launching dialog.
     * It checks if any conflict can be skipped using resolveTagConflictsDefault from TagConflictsUtil
     *
     * @return list of commands as updating tags
     * @throws UserCancelException if user close the window or reject possible tags conflict
     */
    protected List<Command> prepareUpdateTagsCommands(
        Way selectedBuilding,
        Way newBuilding
    ) throws UserCancelException {
        Collection<OsmPrimitive> primitives = Arrays.asList(selectedBuilding, newBuilding);
        TagCollection tagsOfPrimitives = TagCollection.unionOfAllPrimitives(primitives);

        resolveTagConflictsDefault(tagsOfPrimitives, selectedBuilding, newBuilding);

        return CombinePrimitiveResolverDialog.launchIfNecessary(
            tagsOfPrimitives,
            primitives,
            Collections.singleton(selectedBuilding)
        );
    }

    @Override
    public Way getResultBuilding() {
        return this.selectedBuilding;
    }

    @Override
    public String getErrorReason() {
        return executeErrorReason;
    }
}
