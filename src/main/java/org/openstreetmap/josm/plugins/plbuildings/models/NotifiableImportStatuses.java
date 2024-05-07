package org.openstreetmap.josm.plugins.plbuildings.models;

import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.*;

public class NotifiableImportStatuses {
    private static final List<ImportStatus> notifiableStatuses = List.of(NO_DATA,NO_UPDATE, CONNECTION_ERROR, IMPORT_ERROR);

    private static NotifiableImportStatuses instance;
    private final HashMap<ImportStatus, Boolean> enabledNotifications;

    private NotifiableImportStatuses(){
        this.enabledNotifications = new HashMap<>();
        load();
    }

    public static NotifiableImportStatuses getInstance() {
        if (instance == null){
            instance = new NotifiableImportStatuses();
        }
        return instance;
    }

    static void reset() {
        instance = null;
    }

    public boolean isNotifiable(ImportStatus status){
        if (!notifiableStatuses.contains(status)){
            return false;
        }
        return enabledNotifications.getOrDefault(status, true);
    }

    private void load(){
        String rawNotifiableImportStatuses = BuildingsSettings.NOTIFIABLE_IMPORT_STATUSES.get();
        JsonReader jsonReader = Json.createReader(new StringReader(rawNotifiableImportStatuses));
        JsonObject jsonStatuses = jsonReader.readObject();
        jsonReader.close();

        notifiableStatuses.forEach(status -> {
            try {
                enabledNotifications.put(status, jsonStatuses.getBoolean(status.name()));
            } catch (NullPointerException ignore){
                enabledNotifications.put(status, true);
            }
        });
    }

    private void save(){
        JsonObjectBuilder jsonNotifiableStatusBuilder = Json.createObjectBuilder();
        enabledNotifications.forEach(((importStatus, isEnabled) -> jsonNotifiableStatusBuilder.add(importStatus.name(), isEnabled)));
        BuildingsSettings.NOTIFIABLE_IMPORT_STATUSES.put(jsonNotifiableStatusBuilder.build().toString());
    }

    public void setNotifiable(ImportStatus status, boolean enabled){
        if (!notifiableStatuses.contains(status)){
            return;
        }

        enabledNotifications.put(status, enabled);
        save();
    }
}
