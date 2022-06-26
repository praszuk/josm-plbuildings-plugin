package org.openstreetmap.josm.plugins.plbuildings.commands;

import org.openstreetmap.josm.data.osm.Way;

/**
 * Interface to use in the Commands for the "chaining possibility"
 * of created/modified OsmPrimitive with SequenceCommand.
 */
public interface CommandResultBuilding {
    Way getResultBuilding();
}
