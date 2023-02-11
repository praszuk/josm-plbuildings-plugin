package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.testutils.JOSMTestRules;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DataSourceConfigTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    DataSourceConfig dataSourceConfig;
    DataSourceServer server1;
    DataSourceServer server2;
    DataSourceProfile profile1server1;
    DataSourceProfile profile2server1;
    DataSourceProfile profile3server2;

    @Before
    public void setUp(){
        this.dataSourceConfig = DataSourceConfig.getInstance();

        this.server1 = new DataSourceServer("Test", "http://127.0.0.1");
        this.server2 = new DataSourceServer("NotTest", "http://127.0.0.2");

        this.profile1server1 = new DataSourceProfile(
                "Test",
                "test1",
                "test2",
                "test profile1"
        );
        this.profile2server1 = new DataSourceProfile(
                "Test",
                "test1",
                "test2",
                "test profile2"
        );
        this.profile3server2 = new DataSourceProfile(
                "NotTest",
                "test1",
                "test2",
                "test profile3"
        );
    }
    void clearDataSourceConfig(){
        dataSourceConfig.getProfiles().forEach(dataSourceConfig::removeProfile);
        dataSourceConfig.getServers().forEach(dataSourceConfig::removeServer);
    }
    @Test
    public void cannotAddDuplicatedServerByName(){
        clearDataSourceConfig();
        dataSourceConfig.addServer(server1);
        assertThrows(IllegalArgumentException.class, () -> dataSourceConfig.addServer(server1));
        assertEquals(dataSourceConfig.getServers().size(), 1);
    }

    @Test
    public void cannotAddDuplicatedProfileByName(){
        clearDataSourceConfig();
        dataSourceConfig.addServer(server1);

        dataSourceConfig.addProfile(profile1server1);
        assertThrows(IllegalArgumentException.class, () -> dataSourceConfig.addProfile(profile1server1));
        assertEquals(dataSourceConfig.getProfiles().size(), 1);
    }
    @Test
    public void getServerProfilesTest(){
        clearDataSourceConfig();

        dataSourceConfig.addServer(server1);
        dataSourceConfig.addServer(server2);
        dataSourceConfig.addProfile(profile1server1);
        dataSourceConfig.addProfile(profile2server1);
        dataSourceConfig.addProfile(profile3server2);

        assertEquals(dataSourceConfig.getServerProfiles(server1).size(), 2);
        assertEquals(dataSourceConfig.getServerProfiles(server2).size(), 1);
    }

    @Test
    public void removeServerMultipleTimeTest(){
        clearDataSourceConfig();

        dataSourceConfig.addServer(server1);
        assertEquals(dataSourceConfig.getServers().size(), 1);
        dataSourceConfig.removeServer(server1);
        assertEquals(dataSourceConfig.getProfiles().size(), 0);
        dataSourceConfig.removeServer(server1);
        assertEquals(dataSourceConfig.getProfiles().size(), 0);
    }

    @Test
    public void removeProfileMultipleTimeTest(){
        clearDataSourceConfig();

        dataSourceConfig.addServer(server1);
        dataSourceConfig.addProfile(profile1server1);
        assertEquals(dataSourceConfig.getProfiles().size(), 1);
        dataSourceConfig.removeProfile(profile1server1);
        assertEquals(dataSourceConfig.getProfiles().size(), 0);
        dataSourceConfig.removeProfile(profile1server1);
        assertEquals(dataSourceConfig.getProfiles().size(), 0);
    }

    @Test
    public void removeServerCauseRemoveAllConnectedProfiles(){
        clearDataSourceConfig();

        dataSourceConfig.addServer(server1);
        dataSourceConfig.addServer(server2);

        dataSourceConfig.addProfile(profile1server1);
        dataSourceConfig.addProfile(profile2server1);
        dataSourceConfig.addProfile(profile3server2);

        assertEquals(dataSourceConfig.getServers().size(), 2);
        assertEquals(dataSourceConfig.getProfiles().size(), 3);
        dataSourceConfig.removeServer(server1);
        assertEquals(dataSourceConfig.getServers().size(), 1);
        assertEquals(dataSourceConfig.getProfiles().size(), 1);
    }

}
