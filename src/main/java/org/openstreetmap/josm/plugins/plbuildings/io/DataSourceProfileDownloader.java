package org.openstreetmap.josm.plugins.plbuildings.io;

import static org.openstreetmap.josm.plugins.plbuildings.utils.JsonUtil.provider;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.tools.Http1Client;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

public class DataSourceProfileDownloader {
    /**
     * @return profiles collection if response is parsed ok (might be empty), else return null if parse/request error
     */
    public static Collection<DataSourceProfile> downloadProfiles(DataSourceServer server) {
        String rawUrl = server.getUrl() + DownloaderConstants.API_DATA_SOURCES_PROFILES + "/";

        JsonReader reader = null;
        Collection<DataSourceProfile> dataSourcesProfiles = new ArrayList<>();

        try {
            URL url = new URL(rawUrl);
            HttpClient httpClient = new Http1Client(url, "GET");
            httpClient.setConnectTimeout(BuildingsSettings.CONNECTION_TIMEOUT.get());
            httpClient.setHeader("User-Agent", DownloaderConstants.USER_AGENT);
            httpClient.connect();
            HttpClient.Response response = httpClient.getResponse();

            reader = provider.createReader(response.getContent());
            JsonArray objects = reader.readArray();
            for (int i = 0; i < objects.size(); i++) {
                JsonObject ds = objects.getJsonObject(i);

                String name = ds.getString("name");
                String tags = ds.getString("tags");
                String geometry = ds.getString("geometry");

                dataSourcesProfiles.add(new DataSourceProfile(server.getName(), geometry, tags, name));
            }
        } catch (IOException ioException) {
            Logging.warn("Connection error with getting profiles data: {0}", ioException.getMessage());
            dataSourcesProfiles = null;
        } catch (Exception exception) {
            Logging.error("Cannot parse data set from the server: {0}", exception.getMessage());
            dataSourcesProfiles = null;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return dataSourcesProfiles;
    }
}
