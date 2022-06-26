package org.openstreetmap.josm.plugins.plbuildings.commands;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataIntegrityProblemException;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryCommand;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryException;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryUtils;
import org.openstreetmap.josm.tools.Logging;

import javax.swing.*;
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

    public ReplaceUpdateBuildingCommand(DataSet data, Way selectedBuilding, CommandResultBuilding resultNewBuilding) {
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
    }

    @Override
    public boolean executeCommand() {
        this.newBuilding = resultNewBuilding.getResultBuilding();

        try {
            replaceAndUpdate(selectedBuilding, newBuilding);
            Logging.debug("Updated tags for the selected building: {0}", selectedBuilding);
            return true;
        } catch(Exception exception){
            handleException(exception);
            return false;
        }
    }

    private void handleException(Exception exception) {
        Notification note;

        if (exception instanceof IllegalArgumentException) {
            // If user cancel conflict window do nothing
            note = new Notification(tr("Canceled merging buildings."));
            note.setIcon(JOptionPane.WARNING_MESSAGE);

            Logging.debug(
                "No building (id: {0}) update, caused: Cancel conflict dialog by user",
                selectedBuilding.getId()
            );
        } else if(exception instanceof ReplaceGeometryException) {
            // If selected building cannot be merged (e.g. connected ways/relation)
            note = new Notification(tr(
                "Cannot merge buildings!" +
                    " Old building may be connected with some ways/relations" +
                    " or not whole area is downloaded."
            ));
            note.setIcon(JOptionPane.ERROR_MESSAGE);

            Logging.debug(
                "No building update (id: {0}), caused: Replacing Geometry from UtilPlugins2 error",
                selectedBuilding.getId()
            );
        } else if (exception instanceof DataIntegrityProblemException) {
            // If data integrity like nodes duplicated or first!=last has been somehow broken
            note = new Notification(tr(
                "Cannot merge buildings! Building has been wrongly replaced and data has been broken!"
            ));
            note.setIcon(JOptionPane.ERROR_MESSAGE);

            Logging.error(
                "No building update (id: {0}), caused: DataIntegrity with replacing error! Building: {1}",
                selectedBuilding.getId(),
                selectedBuilding
            );
        } else {
            note = new Notification(tr("Cannot merge buildings! Unknown error!"));
            Logging.error(
                "No building update (id: {0}), caused: Unknown error: {1}",
                selectedBuilding.getId(),
                exception.getMessage()
            );
        }
        note.setDuration(Notification.TIME_SHORT);
        note.show();
    }


    /**
     * Main execute command which handle geometry building update
     * @throws IllegalArgumentException if user cancel conflict window
     * @throws NullPointerException if user cancel conflict window
     * @throws ReplaceGeometryException if selected building cannot be merged (e.g. connected ways/relation)
     * @throws DataIntegrityProblemException if something has been broken (low-level) at merging buildings
     */
    private void replaceAndUpdate(Way selectedBuilding, Way newBuilding) throws IllegalArgumentException,
        NullPointerException, ReplaceGeometryException, DataIntegrityProblemException
    {
        replaceGeometryCommand = ReplaceGeometryUtils.buildReplaceWithNewCommand(
            selectedBuilding,
            newBuilding
        );
        replaceGeometryCommand.executeCommand();
        if (!isBuildingWayValid(selectedBuilding)){
            throw new DataIntegrityProblemException("Wrongly merged building!");
        }
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
