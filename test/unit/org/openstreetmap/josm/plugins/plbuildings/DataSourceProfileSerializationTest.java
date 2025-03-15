package org.openstreetmap.josm.plugins.plbuildings;


import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;

public class DataSourceProfileSerializationTest {

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

        Assertions.assertEquals(srcDataSourceProfile.getName(), destDataSourceProfile.getName());
        Assertions.assertEquals(srcDataSourceProfile.getGeometry(), destDataSourceProfile.getGeometry());
        Assertions.assertEquals(srcDataSourceProfile.getTags(), destDataSourceProfile.getTags());
        Assertions.assertEquals(
            srcDataSourceProfile.getDataSourceServerName(),
            destDataSourceProfile.getDataSourceServerName()
        );
    }
}
