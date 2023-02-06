package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.data.Version;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.io.GeoJSONReader;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmJsonReader;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportDataSource;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportDataSourceConfigType;
import org.openstreetmap.josm.plugins.plbuildings.models.ImportDataSourceConfig;
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
import java.util.stream.Collectors;

public class BuildingsDownloader {
    public static final String USER_AGENT = String.format(
        "%s/%s %s",
        BuildingsPlugin.info.name,
        BuildingsPlugin.info.version,
        Version.getInstance().getFullAgentString()
    );
    /**
     * Download buildings from PLBuildings Server API and parse it as DataSet
     * Use default search_distance parameter.
     *
     * @param latLon        location of searching building (EPSG 4386)
     * @param dataSourceCfg dataSource config of buildings.
     * @return BuildingsImportData with downloaded data or empty datasets or empty obj if IO/parse error
     */
    public static BuildingsImportData downloadBuildings(LatLon latLon, ImportDataSourceConfig dataSourceCfg){
        String dataSourceQueryParam = dataSourceCfg.getDataSources()
            .stream()
            .map(ImportDataSource::toString)
            .collect(Collectors.joining(",")).toLowerCase();

        // TODO above will be enough for all types when multiple source will be implemented on the server side
        if (dataSourceCfg.getConfigType() != ImportDataSourceConfigType.SIMPLE){
            Logging.error("Unsupported data source query param: {0}", dataSourceQueryParam);
            return null;
        }

        return downloadBuildings(latLon, dataSourceQueryParam, BuildingsSettings.SEARCH_DISTANCE.get());
    }

    /**
     * Download buildings from PLBuildings Server API and parse it as DataSet
     *
     * @param latLon         location of searching building (EPSG 4386)
     * @param dataSources    dataSources of buildings
     * @param searchDistance distance in meters to find the nearest building from latLon
     * @return BuildingsImportData with downloaded data or empty datasets or empty obj if IO/parse error
     */
    public static BuildingsImportData downloadBuildings(LatLon latLon, String dataSources, Double searchDistance){

        StringBuilder urlBuilder = new StringBuilder(BuildingsSettings.SERVER_URL.get());
        urlBuilder.append("?");
        urlBuilder.append("lat=");
        urlBuilder.append(latLon.lat());

        urlBuilder.append("&");
        urlBuilder.append("lon=");
        urlBuilder.append(latLon.lon());

        urlBuilder.append("&");
        urlBuilder.append("data_sources=");
        urlBuilder.append(dataSources);

        urlBuilder.append("&");
        urlBuilder.append("search_distance=");
        urlBuilder.append(searchDistance);

        Logging.info("Getting building data from: {0}", urlBuilder);

        BuildingsImportData dataSourceBuildingsData = new BuildingsImportData();
        JsonReader reader = null;
        try {
            URL url = new URL(urlBuilder.toString());
            HttpClient httpClient = new Http1Client(url, "GET");
            httpClient.setHeader("User-Agent", USER_AGENT);
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
        } catch (IllegalDataException|ClassCastException exception) {
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
