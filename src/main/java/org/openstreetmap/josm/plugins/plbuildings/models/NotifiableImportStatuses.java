package org.openstreetmap.josm.plugins.plbuildings.models;

import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.CONNECTION_ERROR;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.IMPORT_ERROR;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.NO_DATA;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.NO_UPDATE;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;

public class NotifiableImportStatuses {
    public static final String NOTIFIABLE_IMPORT_STATUSES = "notifiable_import_statuses";
    public static final List<ImportStatus> notifiableStatuses =
        List.of(NO_DATA, NO_UPDATE, CONNECTION_ERROR, IMPORT_ERROR);

    private final HashMap<ImportStatus, Boolean> enabledNotifications;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public NotifiableImportStatuses() {
        this.enabledNotifications = new HashMap<>();
        BuildingsSettings.NOTIFIABLE_IMPORT_STATUSES.addListener(valueChangeEvent -> {
            load();
            propertyChangeSupport.firePropertyChange(NOTIFIABLE_IMPORT_STATUSES, null, enabledNotifications);
        });
        load();
    }

    public static String[] getNotifiableStatusesNames() {
        return notifiableStatuses.stream().map(ImportStatus::toString).toArray(String[]::new);
    }

    public boolean isNotifiable(ImportStatus status) {
        if (!notifiableStatuses.contains(status)) {
            return false;
        }
        return enabledNotifications.getOrDefault(status, true);
    }

    private void load() {
        String rawNotifiableImportStatuses = BuildingsSettings.NOTIFIABLE_IMPORT_STATUSES.get();
        JsonReader jsonReader = Json.createReader(new StringReader(rawNotifiableImportStatuses));
        JsonObject jsonStatuses = jsonReader.readObject();
        jsonReader.close();

        notifiableStatuses.forEach(status -> {
            try {
                enabledNotifications.put(status, jsonStatuses.getBoolean(status.name()));
            } catch (NullPointerException ignore) {
                enabledNotifications.put(status, true);
            }
        });
    }

    private void save() {
        JsonObjectBuilder jsonNotifiableStatusBuilder = Json.createObjectBuilder();
        enabledNotifications.forEach(
            ((importStatus, isEnabled) -> jsonNotifiableStatusBuilder.add(importStatus.name(), isEnabled)));
        BuildingsSettings.NOTIFIABLE_IMPORT_STATUSES.put(jsonNotifiableStatusBuilder.build().toString());
    }

    public void setNotifiable(ImportStatus status, boolean enabled) {
        if (!notifiableStatuses.contains(status)) {
            return;
        }

        enabledNotifications.put(status, enabled);
        save();
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }
}
