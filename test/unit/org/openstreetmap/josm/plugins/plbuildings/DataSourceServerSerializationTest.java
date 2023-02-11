package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Test;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DataSourceServerSerializationTest {
    @Test
    public void serializationDataSourceServerTest(){

        DataSourceServer srcDataSourceServer = new DataSourceServer("Test", "http://127.0.0.1");
        String serialized = DataSourceServer.toJson(List.of(srcDataSourceServer)).toString();
        DataSourceServer destDataSourceServer = new ArrayList<>(DataSourceServer.fromStringJson(serialized)).get(0);

        assertEquals(srcDataSourceServer.getName(), destDataSourceServer.getName());
        assertEquals(srcDataSourceServer.getUrl(), destDataSourceServer.getUrl());
    }
}
