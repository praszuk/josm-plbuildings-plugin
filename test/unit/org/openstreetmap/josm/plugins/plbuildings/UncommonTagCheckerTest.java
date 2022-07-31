package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.utils.PostCheckUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UncommonTagCheckerTest {

    @Test
    public void testPrimitiveHasUncommonTag(){
        OsmPrimitive building1 = new Way();
        building1.put("building", "yes");
        building1.put("amenity", "vehicle_inspection");

        assertTrue(PostCheckUtils.hasUncommonTags(building1));

        OsmPrimitive building2 = new Way();
        building2.put("building", "hotel");
        building2.put("name", "xyz");

        assertTrue(PostCheckUtils.hasUncommonTags(building2));

        OsmPrimitive building3 = new Way();
        building3.put("building", "house");
        building3.put("note", "amenity");
        assertFalse(PostCheckUtils.hasUncommonTags(building3));

        // this value is not in the gugik2osm buildings_categories_mappings it's just "living building"
        OsmPrimitive building4 = new Way();
        building4.put("building", "semidetached_house");
        assertFalse(PostCheckUtils.hasUncommonTags(building4));
    }
}
