package org.openstreetmap.josm.plugins.plbuildings;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class DataSourceProfileSerializationTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void serializationDataSourceProfileTest() {

        DataSourceProfile srcDataSourceProfile = new DataSourceProfile(
            "Test",
            "test1",
            "test2",
            "test profile"
        );
        String serialized = DataSourceProfile.toJson(List.of(srcDataSourceProfile)).toString();
        DataSourceProfile destDataSourceProfile = new ArrayList<>(DataSourceProfile.fromStringJson(serialized)).get(0);

        assertEquals(srcDataSourceProfile.getName(), destDataSourceProfile.getName());
        assertEquals(srcDataSourceProfile.getGeometry(), destDataSourceProfile.getGeometry());
        assertEquals(srcDataSourceProfile.getTags(), destDataSourceProfile.getTags());
        assertEquals(srcDataSourceProfile.getDataSourceServerName(), destDataSourceProfile.getDataSourceServerName());
    }
}
