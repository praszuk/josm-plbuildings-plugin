package org.openstreetmap.josm.plugins.plbuildings.io;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.io.GeoJSONReader;
import org.openstreetmap.josm.io.OsmJsonReader;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.tools.Http1Client;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

public class BuildingsDownloader {
    /**
     * Download buildings from PLBuildings Server API and parse it as DataSet
     *
     * @param manager  ImportManager which contains DataSourceConfig for current download.
     * @return BuildingsImportData with downloaded data or empty datasets or empty obj if IO/parse error
     */
    public static BuildingsImportData downloadBuildings(BuildingsImportManager manager){
        DataSourceProfile currentProfile = manager.getDataSourceProfile();

        String dataSourceQueryParam = currentProfile.getGeometry();
        if (!currentProfile.getGeometry().equals(currentProfile.getTags())){
            dataSourceQueryParam += "," + currentProfile.getTags();
        }
        dataSourceQueryParam = dataSourceQueryParam.toLowerCase();

        String serverUrl = DataSourceConfig
                .getInstance()
                .getServerByName(currentProfile.getDataSourceServerName())
                .getUrl();

        return downloadBuildings(serverUrl, manager.getCursorLatLon(), dataSourceQueryParam);
    }

    /**
     * Download buildings from PLBuildings Server API and parse it as DataSet
     *
     * @param serverUrl      root url to server api e.g. "http://127.0.0.1/api/v1"
     * @param latLon         location of searching building (EPSG 4326)
     * @param dataSources    dataSources of buildings separated with comma
     * @return BuildingsImportData with downloaded data or empty datasets or empty obj if IO/parse error
     */
    public static BuildingsImportData downloadBuildings(String serverUrl, LatLon latLon, String dataSources){
        StringBuilder urlBuilder = new StringBuilder(serverUrl);
        urlBuilder.append(DownloaderConstants.API_BUILDING_AT);

        urlBuilder.append("?");
        urlBuilder.append("lat=");
        urlBuilder.append(latLon.lat());

        urlBuilder.append("&");
        urlBuilder.append("lon=");
        urlBuilder.append(latLon.lon());

        urlBuilder.append("&");
        urlBuilder.append("data_sources=");
        urlBuilder.append(dataSources);

        Logging.info("Getting building data from: {0}", urlBuilder);

        BuildingsImportData dataSourceBuildingsData = new BuildingsImportData();
        JsonReader reader = null;
        try {
            URL url = new URL(urlBuilder.toString());
            HttpClient httpClient = new Http1Client(url, "GET");
            httpClient.setHeader("User-Agent", DownloaderConstants.USER_AGENT);
            httpClient.connect();
            HttpClient.Response response = httpClient.getResponse();

            reader = Json.createReader(response.getContent());
            JsonArray objects = reader.readArray();
            for (int i = 0; i < objects.size(); i++){
                JsonObject ds = objects.getJsonObject(i);

                String source = ds.getString("source");
                String format = ds.getString("format");
                String data = ds.getJsonObject("data").toString();

                // TODO add check if source is in all available data sources

                if (format.equals("geojson")){
                    dataSourceBuildingsData.add(
                        source,
                        GeoJSONReader.parseDataSet(new ByteArrayInputStream(data.getBytes()), null)
                    );
                }else if(format.equals("osmjson")){
                    dataSourceBuildingsData.add(
                        source,
                        OsmJsonReader.parseDataSet(new ByteArrayInputStream(data.getBytes()), null)
                    );
                } else {
                    Logging.error("Downloading error: Incorrect data format!");
                }
            }
        } catch (IOException ioException) {
            Logging.warn("Connection error with getting building data: {0}", ioException.getMessage());
        } catch (Exception exception) {
            Logging.error("Cannot parse data set from the server: {0}", exception.getMessage());
        }
        finally {
            if (reader != null){
                reader.close();
            }
        }
        return dataSourceBuildingsData;
    }
}
