package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.tools.Logging;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;


/**
 * BuildingsImportStats keeps simple importing statics
 * and manages serialization/deserialization between BuildingSettings
 */
public class BuildingsImportStats {
    private int importCounter;
    private int importWithReplaceCounter;

    private static BuildingsImportStats instance;

    public static BuildingsImportStats getInstance(){
        if (instance == null){
            instance = new BuildingsImportStats();
        }
        return instance;
    }

    public void addImportCounter(int value){
        if (value < 1){
            throw new IllegalArgumentException("Number must be greater than 0");
        }
        this.importCounter += value;
        save();
    }

    public void addImportWithReplaceCounter(int value){
        if (value < 1){
            throw new IllegalArgumentException("Number must be greater than 0");
        }
        this.importWithReplaceCounter += value;
        save();
    }

    public HashMap<String, Object> getStats(){
        HashMap<String, Object> stats = new HashMap<>();
        stats.put("import", this.importCounter);
        stats.put("importWithReplace", this.importWithReplaceCounter);

        return stats;
    }

    @Override
    public String toString() {
        return getStats().toString();
    }

    private BuildingsImportStats(){
        load();
    }

    /**
     * Saves data to JOSM settings
     */
    private void save(){
        Logging.debug("Saving import stats: {0}", this.toString());
        JsonObject jsonStats = Json.createObjectBuilder()
            .add("import", this.importCounter)
            .add("importWithReplace", this.importWithReplaceCounter)
            .build();

        String encodedB64Stats = Base64.getEncoder().encodeToString(
            jsonStats.toString().getBytes(StandardCharsets.UTF_8)
        );
        BuildingsSettings.IMPORT_STATS.put(encodedB64Stats);
    }

    /**
     * Loads data from JOSM settings
     * decode from base64 to json string and then map to fields
     */
    private void load(){

        String encodedB64Stats = BuildingsSettings.IMPORT_STATS.get();
        String decodedJsonStats = new String(Base64.getDecoder().decode(encodedB64Stats));
        JsonReader jsonReader = Json.createReader(new StringReader(decodedJsonStats));
        JsonObject jsonStats = jsonReader.readObject();
        jsonReader.close();

        this.importCounter = jsonStats.getInt("import", 0);
        this.importWithReplaceCounter = jsonStats.getInt("importWithReplace", 0);
        Logging.debug("Loaded import stats: {0}", this.toString());
    }
}
