package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.utils.BuildingsOverlapDetector.detect;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.data.projection.Projections;

public class BuildingsOverlappingTest {
    @BeforeEach
    public void setUp() {
        ProjectionRegistry.setProjection(Projections.getProjectionByCode("EPSG:4326"));
        // System.out.println(ProjectionRegistry.getProjection()); -- Prints: WGS 84
    }

    @Test
    public void testCrossOverlappingOver50percent() {
        DataSet ds = importOsmFile(new File("test/data/overlapping/crossing_over_50.osm"), "");
        Assertions.assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        Assertions.assertNotNull(b1);
        Assertions.assertNotNull(b2);

        Assertions.assertTrue(detect(b1, b2) > 50.);
    }

    @Test
    public void testCrossOverlappingSecondOverlappedAbout20percent() {
        DataSet ds = importOsmFile(new File("test/data/overlapping/crossing_about_20.osm"), "");
        Assertions.assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        Assertions.assertNotNull(b1);
        Assertions.assertNotNull(b2);

        Assertions.assertEquals(25., detect(b1, b2), 5);
        Assertions.assertEquals(25., detect(b2, b1), 5);
    }


    @Test
    public void testFirstInSecondOverlappingOver50percent() {
        DataSet ds = importOsmFile(new File("test/data/overlapping/first_in_second_over_50.osm"), "");
        Assertions.assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        Assertions.assertNotNull(b1);
        Assertions.assertNotNull(b2);

        Assertions.assertTrue(detect(b1, b2) > 50.);
    }

    @Test
    public void testSecondInFirstOverlappingOver50percent() {
        DataSet ds = importOsmFile(new File("test/data/overlapping/second_in_first_over_50.osm"), "");
        Assertions.assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        Assertions.assertNotNull(b1);
        Assertions.assertNotNull(b2);

        Assertions.assertTrue(detect(b1, b2) > 50.);
    }

    @Test
    public void testNoIntersection() {
        DataSet ds = importOsmFile(new File("test/data/overlapping/not_intersection.osm"), "");
        Assertions.assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        Assertions.assertNotNull(b1);
        Assertions.assertNotNull(b2);

        Assertions.assertEquals(0., detect(b1, b2), 0.1);
    }

    @Test
    public void testCrossOverlappingLess10percent() {
        DataSet ds = importOsmFile(new File("test/data/overlapping/crossing_less_10.osm"), "");
        Assertions.assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        Assertions.assertNotNull(b1);
        Assertions.assertNotNull(b2);

        Assertions.assertTrue(detect(b1, b2) < 10.);
    }

    @Test
    public void testCrossOverlappingOver90percent() {
        DataSet ds = importOsmFile(new File("test/data/overlapping/crossing_over_90.osm"), "");
        Assertions.assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        Assertions.assertNotNull(b1);
        Assertions.assertNotNull(b2);

        Assertions.assertTrue(detect(b1, b2) > 90.);
    }

    @Test
    public void testCrossOverlapping100percent() {
        DataSet ds = importOsmFile(new File("test/data/overlapping/same_coordinates.osm"), "");
        Assertions.assertNotNull(ds);

        Way b1 = ds.getWays().stream().filter(w -> w.hasTag("name", "1")).findFirst().orElse(null);
        Way b2 = ds.getWays().stream().filter(w -> w.hasTag("name", "2")).findFirst().orElse(null);
        Assertions.assertNotNull(b1);
        Assertions.assertNotNull(b2);

        Assertions.assertEquals(100.0, detect(b1, b2), 1);
    }
}
