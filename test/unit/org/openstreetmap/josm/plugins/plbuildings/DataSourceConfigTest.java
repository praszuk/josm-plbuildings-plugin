package org.openstreetmap.josm.plugins.plbuildings;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.plugins.plbuildings.io.DataSourceProfileDownloader;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
    public void cannotAddDuplicatedServerByNameTest(){
        clearDataSourceConfig();
        dataSourceConfig.addServer(server1);
        assertThrows(IllegalArgumentException.class, () -> dataSourceConfig.addServer(server1));
        assertEquals(dataSourceConfig.getServers().size(), 1);
    }

    @Test
    public void cannotAddDuplicatedProfileByNameTest(){
        clearDataSourceConfig();
        dataSourceConfig.addServer(server1);

        dataSourceConfig.addProfile(profile1server1);
        assertThrows(IllegalArgumentException.class, () -> dataSourceConfig.addProfile(profile1server1));
        assertEquals(dataSourceConfig.getProfiles().size(), 1);
    }

    @Test
    public void canAddDuplicatedProfileNameForDifferentServerTest(){
        clearDataSourceConfig();
        dataSourceConfig.addServer(server1);
        dataSourceConfig.addServer(server2);

        dataSourceConfig.addProfile(profile1server1);
        DataSourceProfile duplicatedProfile = new DataSourceProfile(
                server2.getName(),
                profile1server1.getGeometry(),
                profile1server1.getTags(),
                profile1server1.getName()
        );
        dataSourceConfig.addProfile(duplicatedProfile);
        assertEquals(dataSourceConfig.getProfiles().size(), 2);
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
    public void removeServerCauseRemoveAllConnectedProfilesTest(){
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

    @Test
    public void refreshProfileWhichNotExistInNewProfilesWillBeRemovedFromConfigTest(){
        clearDataSourceConfig();

        dataSourceConfig.addServer(server1);

        dataSourceConfig.addProfile(profile1server1);
        dataSourceConfig.addProfile(profile2server1);

        new MockUp<DataSourceProfileDownloader>(){
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server){
                return Collections.singletonList(profile2server1);
            }
        };

        dataSourceConfig.refresh(false);
        assertEquals(dataSourceConfig.getProfiles().size(), 1);
    }

    @Test
    public void refreshProfileWhichUpdatedFieldsWillBeUpdatedTest(){
        clearDataSourceConfig();

        dataSourceConfig.addServer(server1);

        dataSourceConfig.addProfile(profile1server1);
        dataSourceConfig.addProfile(profile2server1);

        DataSourceProfile modifiedDataSourceProfile = new DataSourceProfile(
                profile2server1.getDataSourceServerName(),
                profile2server1.getGeometry() + "modified",
                profile2server1.getTags() + "modified",
                profile2server1.getName()
        );

        new MockUp<DataSourceProfileDownloader>(){
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server){
                return Arrays.asList(profile1server1, modifiedDataSourceProfile);
            }
        };

        dataSourceConfig.refresh(false);
        assertEquals(dataSourceConfig.getProfiles().size(), 2);

        DataSourceProfile expectedUpdatedProfile = dataSourceConfig.getProfileByName(
                profile2server1.getDataSourceServerName(),
                profile2server1.getName()
        );
        assertEquals(expectedUpdatedProfile.getGeometry(), modifiedDataSourceProfile.getGeometry());

    }

    @Test
    public void refreshProfileWhichNotExistConfigWillBeAddedTest(){
        clearDataSourceConfig();

        dataSourceConfig.addServer(server1);
        dataSourceConfig.addServer(server2);

        dataSourceConfig.addProfile(profile1server1);

        new MockUp<DataSourceProfileDownloader>(){
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server){
                if (server.getName().equals(server1.getName())){
                    return Arrays.asList(profile1server1, profile2server1);
                }else if (server.getName().equals(server2.getName())){
                    return Collections.singletonList(profile3server2);
                }
                return null;
            }
        };

        dataSourceConfig.refresh(false);
        assertEquals(dataSourceConfig.getProfiles().size(), 3);
    }

    @Test
    public void refreshProfilesNullResponseForOneServerShouldNotChangeAnythingInThisServerConfigTest(){
        clearDataSourceConfig();

        dataSourceConfig.addServer(server1);
        dataSourceConfig.addServer(server2);

        dataSourceConfig.addProfile(profile1server1);
        dataSourceConfig.addProfile(profile2server1);

        DataSourceProfile newProfile4Server2 = new DataSourceProfile(
                server2.getName(),
                "test",
                "test",
                profile3server2.getName() + "1"
        );

        new MockUp<DataSourceProfileDownloader>(){
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server){
                if (server.getName().equals(server1.getName())){
                    return null;
                }else if (server.getName().equals(server2.getName())){
                    return Arrays.asList(profile3server2, newProfile4Server2);
                }
                return null;
            }
        };

        dataSourceConfig.refresh(false);
        assertEquals(dataSourceConfig.getProfiles().size(), 4);
    }

    @Test
    public void refreshProfileUpdateDoesNotChangeTheOrderOfProfilesTest(){
        clearDataSourceConfig();

        DataSourceProfile profile3server1 = new DataSourceProfile(
            server1.getName(),
            profile1server1.getGeometry(),
            profile1server1.getTags(),
            "profile3server1"
        );

        dataSourceConfig.addServer(server1);

        ArrayList<DataSourceProfile> correctOrder = new ArrayList<>(Arrays.asList(
                profile1server1, profile2server1, profile3server1
        ));

        ArrayList<DataSourceProfile> remoteOrder = new ArrayList<>(Arrays.asList(
                profile3server1, profile2server1, profile1server1
        ));
        correctOrder.forEach(dataSourceConfig::addProfile);

        assertNotEquals(correctOrder, remoteOrder);
        new MockUp<DataSourceProfileDownloader>(){
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server){
                return remoteOrder;
            }
        };

        dataSourceConfig.refresh(false);
        assertEquals(dataSourceConfig.getProfiles(), correctOrder);
        assertNotEquals(dataSourceConfig.getProfiles(), remoteOrder);
    }
}
