package org.openstreetmap.josm.plugins.plbuildings.io;

import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.tools.Http1Client;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public class DataSourceProfileDownloader {
    public static Collection<DataSourceProfile> downloadProfiles(DataSourceServer server){
        String rawUrl = server.getUrl() + DownloaderConstants.API_DATA_SOURCES_PROFILES;

        JsonReader reader = null;
        Collection <DataSourceProfile> dataSourcesProfiles = new ArrayList<>();

        try {
            URL url = new URL(rawUrl);
            HttpClient httpClient = new Http1Client(url, "GET");
            httpClient.setHeader("User-Agent", DownloaderConstants.USER_AGENT);
            httpClient.connect();
            HttpClient.Response response = httpClient.getResponse();

            reader = Json.createReader(response.getContent());
            JsonArray objects = reader.readArray();
            for (int i = 0; i < objects.size(); i++){
                JsonObject ds = objects.getJsonObject(i);

                String name = ds.getString("name");
                String tags = ds.getString("tags");
                String geometry = ds.getString("geometry");

                dataSourcesProfiles.add(new DataSourceProfile(server.getName(), geometry, tags, name));
            }
        } catch (IOException ioException) {
            Logging.warn("Connection error with getting profiles data: {0}", ioException.getMessage());
        } catch (IllegalArgumentException | ClassCastException exception) {
            Logging.error("Cannot parse data set from the server: {0}", exception.getMessage());
        }
        finally {
            if (reader != null){
                reader.close();
            }
        }
        return dataSourcesProfiles;
    }
}
