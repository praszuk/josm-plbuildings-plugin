package org.openstreetmap.josm.plugins.plbuildings.models;

import static org.openstreetmap.josm.plugins.plbuildings.utils.JsonUtil.provider;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.enums.Notification;

public class NotificationConfig {
    public static final String NOTIFICATION_STATE_CHANGED = "notification_state_changed";

    private final LinkedHashMap<Notification, Boolean> notificationStates;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public NotificationConfig() {
        this.notificationStates = new LinkedHashMap<>();
        BuildingsSettings.NOTIFICATION_STATES.addListener(valueChangeEvent -> {
            load();
            propertyChangeSupport.firePropertyChange(NOTIFICATION_STATE_CHANGED, null, notificationStates);
        });
        load();
    }

    private void load() {
        String rawNotificationStates = BuildingsSettings.NOTIFICATION_STATES.get();
        JsonReader jsonReader = provider.createReader(new StringReader(rawNotificationStates));
        JsonArray jsonArray = jsonReader.readArray();

        List<Notification> allNotifications = Arrays.stream(Notification.values()).collect(Collectors.toList());
        List<String> allNotificationNames = allNotifications.stream()
            .map(Notification::getName)
            .collect(Collectors.toList());

        for (JsonValue jsonValue : jsonArray) {
            JsonObject jsonObject = jsonValue.asJsonObject();
            String notificationName = jsonObject.getString("name");

            if (allNotificationNames.contains(notificationName)) {
                Notification notification = Notification.fromName(notificationName);
                notificationStates.put(notification, jsonObject.getBoolean("isEnabled"));
                allNotifications.remove(notification);
            }
        }
        jsonReader.close();

        // Should be empty, it will be executed only on init or notification changes (in code)
        if (!allNotifications.isEmpty()) {
            allNotifications.forEach(n -> notificationStates.put(n, true));
            save();
        }
    }

    private void save() {
        JsonArrayBuilder builder = provider.createArrayBuilder();
        notificationStates.forEach((notification, isEnabled) -> builder.add(
            provider.createObjectBuilder()
                .add("name", notification.getName())
                .add("isEnabled", isEnabled)
                .build()
        ));
        BuildingsSettings.NOTIFICATION_STATES.put(builder.build().toString());
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }

    public boolean isNotificationEnabled(Notification notification) {
        return notificationStates.get(notification);
    }

    public void setNotificationEnabled(Notification notification, boolean enabled) {
        notificationStates.put(notification, enabled);
        save();
    }

}
