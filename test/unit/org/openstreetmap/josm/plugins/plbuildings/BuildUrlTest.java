package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.io.BuildingsDownloader.buildUrl;

import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;

public class BuildUrlTest {

    @Test
    void testBuildUrlLocale() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();

        DataSourceServer server = new DataSourceServer("Test server", "http://example.com");
        DataSourceProfile profile = new DataSourceProfile(
            "Test server",
            "geometry_name",
            "tag_name",
            "test profile",
            true
        );
        dataSourceConfig.addServer(server);
        dataSourceConfig.addProfile(profile);
        dataSourceConfig.setCurrentProfile(profile);

        LatLon latLon = new LatLon(52.231, 21.123);

        Locale.setDefault(new Locale("en", "US"));
        Assertions.assertEquals(
            "http://example.com/buildings/?lat=52.231000&lon=21.123000&data_sources=geometry_name,tag_name",
            buildUrl(dataSourceConfig, latLon, dataSourceConfig.getCurrentProfile())
        );

        Locale.setDefault(new Locale("pl", "PL")); // Poland uses commas for decimal numbers
        Assertions.assertEquals(
            "http://example.com/buildings/?lat=52.231000&lon=21.123000&data_sources=geometry_name,tag_name",
            buildUrl(dataSourceConfig, latLon, dataSourceConfig.getCurrentProfile())
        );
    }
}
