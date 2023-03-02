package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.utils.BuildingsOverlapDetector.detect;

public class BuildingsOverlappingTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();


    @Before
    public void setUp(){
        ProjectionRegistry.setProjection(Projections.getProjectionByCode("EPSG:4326"));
        // System.out.println(ProjectionRegistry.getProjection()); -- Prints: WGS 84
    }
    @Test
    public void testCrossOverlappingOver50percent(){
        DataSet ds = importOsmFile(new File("test/data/overlapping/crossing_over_50.osm"), "");
        assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        assertNotNull(b1);
        assertNotNull(b2);

        assertTrue(detect(b1, b2) > 50.);
    }
    @Test
    public void testFirstInSecondOverlappingOver50percent(){
        DataSet ds = importOsmFile(new File("test/data/overlapping/first_in_second_over_50.osm"), "");
        assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        assertNotNull(b1);
        assertNotNull(b2);

        assertTrue(detect(b1, b2) > 50.);
    }

    @Test
    public void testSecondInFirstOverlappingOver50percent(){
        DataSet ds = importOsmFile(new File("test/data/overlapping/second_in_first_over_50.osm"), "");
        assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        assertNotNull(b1);
        assertNotNull(b2);

        assertTrue(detect(b1, b2) > 50.);
    }

    @Test
    public void testNoIntersection(){
        DataSet ds = importOsmFile(new File("test/data/overlapping/not_intersection.osm"), "");
        assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        assertNotNull(b1);
        assertNotNull(b2);

        assertEquals(0., detect(b1, b2), 0.1);
    }

    @Test
    public void testCrossOverlappingLess10percent(){
        DataSet ds = importOsmFile(new File("test/data/overlapping/crossing_less_10.osm"), "");
        assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        assertNotNull(b1);
        assertNotNull(b2);

        assertTrue(detect(b1, b2) < 10.);
    }
    @Test
    public void testCrossOverlappingOver90percent(){
        DataSet ds = importOsmFile(new File("test/data/overlapping/crossing_over_90.osm"), "");
        assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        assertNotNull(b1);
        assertNotNull(b2);

        assertTrue(detect(b1, b2) > 90.);
    }

    @Test
    public void testCrossOverlapping100percent(){
        DataSet ds = importOsmFile(new File("test/data/overlapping/same_coordinates.osm"), "");
        assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        assertNotNull(b1);
        assertNotNull(b2);

        assertEquals(100.0, detect(b1, b2), 1);
    }
}
