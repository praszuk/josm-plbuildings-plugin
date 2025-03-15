package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils;

public class SurveyCheckerTest {
    @Test
    public void testPrimitiveHasSurveyKeyValue() {
        OsmPrimitive primitive1 = new Node();
        primitive1.put("shop", "convenience");
        primitive1.put("name", "xyz");
        primitive1.put("source", "survey");

        Assertions.assertTrue(PreCheckUtils.hasSurveyValue(primitive1));

        OsmPrimitive primitive2 = new Way();
        primitive2.put("highway", "residential");
        primitive2.put("name", "xyz2");
        primitive2.put("note", "Name is obtained from my survey.");

        Assertions.assertTrue(PreCheckUtils.hasSurveyValue(primitive2));

        OsmPrimitive primitive3 = new Node();
        primitive3.put("amenity", "bench");
        primitive3.put("survey:date", "2022-01-01");
        primitive3.put("backrest", "yes");

        Assertions.assertTrue(PreCheckUtils.hasSurveyValue(primitive3));

        OsmPrimitive primitive4 = new Way();
        primitive4.put("highway", "service");
        Assertions.assertFalse(PreCheckUtils.hasSurveyValue(primitive4));
    }
}
