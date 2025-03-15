package org.openstreetmap.josm.plugins.plbuildings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.plugins.plbuildings.io.DataSourceProfileDownloader;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;

public class DataSourceConfigTest {
    DataSourceConfig dataSourceConfig;
    DataSourceServer server1;
    DataSourceServer server2;
    DataSourceProfile profile1server1;
    DataSourceProfile profile2server1;
    DataSourceProfile profile3server2;

    @BeforeEach
    public void setUp() {
        BuildingsSettings.DATA_SOURCE_SERVERS.put("[]");
        BuildingsSettings.DATA_SOURCE_PROFILES.put("[]");
        BuildingsSettings.CURRENT_DATA_SOURCE_PROFILE.put(null);

        this.dataSourceConfig = new DataSourceConfig();

        this.server1 = new DataSourceServer("Test", "http://127.0.0.1");
        this.server2 = new DataSourceServer("NotTest", "http://127.0.0.2");

        this.profile1server1 = new DataSourceProfile(
            "Test",
            "test1",
            "test2",
            "test profile1",
            true
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
            "test profile3",
            false
        );
    }

    @Test
    public void cannotAddDuplicatedServerByNameTest() {
        dataSourceConfig.addServer(server1);
        assertThrows(IllegalArgumentException.class, () -> dataSourceConfig.addServer(server1));
        assertEquals(1, dataSourceConfig.getServers().size());
    }

    @Test
    public void cannotAddDuplicatedProfileByNameTest() {
        dataSourceConfig.addServer(server1);

        dataSourceConfig.addProfile(profile1server1);
        assertThrows(IllegalArgumentException.class, () -> dataSourceConfig.addProfile(profile1server1));
        assertEquals(1, dataSourceConfig.getProfiles().size());
    }

    @Test
    public void canAddDuplicatedProfileNameForDifferentServerTest() {
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
        assertEquals(2, dataSourceConfig.getProfiles().size());
    }

    @Test
    public void getServerProfilesTest() {
        dataSourceConfig.addServer(server1);
        dataSourceConfig.addServer(server2);
        dataSourceConfig.addProfile(profile1server1);
        dataSourceConfig.addProfile(profile2server1);
        dataSourceConfig.addProfile(profile3server2);

        assertEquals(2, dataSourceConfig.getServerProfiles(server1).size());
        assertEquals(1, dataSourceConfig.getServerProfiles(server2).size());
    }

    @Test
    public void removeServerMultipleTimeTest() {
        dataSourceConfig.addServer(server1);
        assertEquals(1, dataSourceConfig.getServers().size());
        dataSourceConfig.removeServer(server1);
        assertEquals(0, dataSourceConfig.getProfiles().size());
        dataSourceConfig.removeServer(server1);
        assertEquals(0, dataSourceConfig.getProfiles().size());
    }

    @Test
    public void removeProfileMultipleTimeTest() {
        dataSourceConfig.addServer(server1);
        dataSourceConfig.addProfile(profile1server1);
        assertEquals(1, dataSourceConfig.getProfiles().size());
        dataSourceConfig.removeProfile(profile1server1);
        assertEquals(0, dataSourceConfig.getProfiles().size());
        dataSourceConfig.removeProfile(profile1server1);
        assertEquals(0, dataSourceConfig.getProfiles().size());
    }

    @Test
    public void removeServerCauseRemoveAllConnectedProfilesTest() {
        dataSourceConfig.addServer(server1);
        dataSourceConfig.addServer(server2);

        dataSourceConfig.addProfile(profile1server1);
        dataSourceConfig.addProfile(profile2server1);
        dataSourceConfig.addProfile(profile3server2);

        assertEquals(2, dataSourceConfig.getServers().size());
        assertEquals(3, dataSourceConfig.getProfiles().size());
        dataSourceConfig.removeServer(server1);
        assertEquals(1, dataSourceConfig.getServers().size());
        assertEquals(1, dataSourceConfig.getProfiles().size());
    }

    @Test
    public void refreshFromServerProfileWhichNotExistInNewProfilesWillBeRemovedFromConfigTest() {
        dataSourceConfig.addServer(server1);

        dataSourceConfig.addProfile(profile1server1);
        dataSourceConfig.addProfile(profile2server1);

        new MockUp<DataSourceProfileDownloader>() {
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server) {
                return Collections.singletonList(profile2server1);
            }
        };

        dataSourceConfig.refreshFromServer(false);
        assertEquals(1, dataSourceConfig.getProfiles().size());
    }

    @Test
    public void refreshFromServerProfileWhichUpdatedFieldsWillBeUpdatedTest() {
        dataSourceConfig.addServer(server1);

        dataSourceConfig.addProfile(profile1server1);
        dataSourceConfig.addProfile(profile2server1);

        DataSourceProfile modifiedDataSourceProfile = new DataSourceProfile(
            profile2server1.getDataSourceServerName(),
            profile2server1.getGeometry() + "modified",
            profile2server1.getTags() + "modified",
            profile2server1.getName()
        );

        new MockUp<DataSourceProfileDownloader>() {
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server) {
                return Arrays.asList(profile1server1, modifiedDataSourceProfile);
            }
        };

        dataSourceConfig.refreshFromServer(false);
        assertEquals(2, dataSourceConfig.getProfiles().size());

        DataSourceProfile expectedUpdatedProfile = dataSourceConfig.getProfileByName(
            profile2server1.getDataSourceServerName(),
            profile2server1.getName()
        );
        assertEquals(expectedUpdatedProfile.getGeometry(), modifiedDataSourceProfile.getGeometry());
    }

    @Test
    public void refreshFromServerProfileWontChangeVisibilityWhenProfileIsLocallyVisibleAndRemotelyInvisibleTest() {
        dataSourceConfig.addServer(server1);
        DataSourceProfile visibleProfile = new DataSourceProfile(
            server1.getName(),
            profile1server1.getGeometry(),
            profile1server1.getTags(),
            profile1server1.getName(),
            true
        );

        dataSourceConfig.addProfile(visibleProfile);
        DataSourceProfile modifiedVisibleProfile = new DataSourceProfile(
            visibleProfile.getDataSourceServerName(),
            visibleProfile.getGeometry() + "modified",
            visibleProfile.getTags() + "modified",
            visibleProfile.getName(),
            false
        );

        new MockUp<DataSourceProfileDownloader>() {
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server) {
                return List.of(modifiedVisibleProfile);
            }
        };

        dataSourceConfig.refreshFromServer(false);
        assertEquals(1, dataSourceConfig.getProfiles().size());

        DataSourceProfile expectedUpdatedVisibleProfile = dataSourceConfig.getProfileByName(
            visibleProfile.getDataSourceServerName(),
            visibleProfile.getName()
        );
        assertTrue(expectedUpdatedVisibleProfile.isVisible());
    }

    @Test
    public void refreshFromServerProfileWontChangeVisibilityWhenProfileIsLocallyInvisibleAndRemotelyVisibleTest() {
        dataSourceConfig.addServer(server1);
        DataSourceProfile invisibleProfile = new DataSourceProfile(
            server1.getName(),
            profile1server1.getGeometry(),
            profile1server1.getTags(),
            profile1server1.getName(),
            false
        );

        dataSourceConfig.addProfile(invisibleProfile);
        DataSourceProfile modifiedInvisibleProfile = new DataSourceProfile(
            invisibleProfile.getDataSourceServerName(),
            invisibleProfile.getGeometry() + "modified",
            invisibleProfile.getTags() + "modified",
            invisibleProfile.getName(),
            true
        );

        new MockUp<DataSourceProfileDownloader>() {
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server) {
                return List.of(modifiedInvisibleProfile);
            }
        };

        dataSourceConfig.refreshFromServer(false);
        assertEquals(1, dataSourceConfig.getProfiles().size());

        DataSourceProfile expectedUpdatedInvisibleProfile = dataSourceConfig.getProfileByName(
            invisibleProfile.getDataSourceServerName(),
            invisibleProfile.getName()
        );
        assertFalse(expectedUpdatedInvisibleProfile.isVisible());
    }

    @Test
    public void refreshFromServerProfileWhichNotExistConfigWillBeAddedTest() {
        dataSourceConfig.addServer(server1);
        dataSourceConfig.addServer(server2);

        dataSourceConfig.addProfile(profile1server1);

        new MockUp<DataSourceProfileDownloader>() {
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server) {
                if (server.getName().equals(server1.getName())) {
                    return Arrays.asList(profile1server1, profile2server1);
                } else if (server.getName().equals(server2.getName())) {
                    return Collections.singletonList(profile3server2);
                }
                return null;
            }
        };

        dataSourceConfig.refreshFromServer(false);
        assertEquals(3, dataSourceConfig.getProfiles().size());
    }

    @Test
    public void refreshFromServerProfilesNullResponseForOneServerShouldNotChangeAnythingInThisServerConfigTest() {
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

        new MockUp<DataSourceProfileDownloader>() {
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server) {
                if (server.getName().equals(server1.getName())) {
                    return null;
                } else if (server.getName().equals(server2.getName())) {
                    return Arrays.asList(profile3server2, newProfile4Server2);
                }
                return null;
            }
        };

        dataSourceConfig.refreshFromServer(false);
        assertEquals(4, dataSourceConfig.getProfiles().size());
    }

    @Test
    public void refreshFromServerProfileUpdateDoesNotChangeTheOrderOfProfilesTest() {
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
        new MockUp<DataSourceProfileDownloader>() {
            @Mock
            public Collection<DataSourceProfile> downloadProfiles(DataSourceServer server) {
                return remoteOrder;
            }
        };

        dataSourceConfig.refreshFromServer(false);
        assertEquals(correctOrder, dataSourceConfig.getProfiles());
        assertNotEquals(remoteOrder, dataSourceConfig.getProfiles());
    }

    @Test
    public void swapProfileOrderTest() {
        dataSourceConfig.addServer(server1);
        dataSourceConfig.addServer(server2);

        ArrayList<DataSourceProfile> currentOrder = new ArrayList<>(Arrays.asList(
            profile1server1, profile2server1, profile3server2
        ));
        ArrayList<DataSourceProfile> expectedOrder = new ArrayList<>(Arrays.asList(
            profile3server2, profile2server1, profile1server1
        ));
        assertNotEquals(expectedOrder, currentOrder);
        currentOrder.forEach(dataSourceConfig::addProfile);


        dataSourceConfig.swapProfileOrder(profile1server1, profile3server2);

        assertEquals(expectedOrder, dataSourceConfig.getProfiles());
    }
}
