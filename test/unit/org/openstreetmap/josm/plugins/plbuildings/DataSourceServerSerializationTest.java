package org.openstreetmap.josm.plugins.plbuildings;


import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;

public class DataSourceServerSerializationTest {
//    @Rule  TODO
//    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void serializationDataSourceServerTest() {

        DataSourceServer srcDataSourceServer = new DataSourceServer("Test", "http://127.0.0.1");
        String serialized = DataSourceServer.toJson(List.of(srcDataSourceServer)).toString();
        DataSourceServer destDataSourceServer = new ArrayList<>(DataSourceServer.fromStringJson(serialized)).get(0);

        Assertions.assertEquals(srcDataSourceServer.getName(), destDataSourceServer.getName());
        Assertions.assertEquals(srcDataSourceServer.getUrl(), destDataSourceServer.getUrl());
    }
}
