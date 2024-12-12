package org.openstreetmap.josm.plugins.plbuildings.models;


import static org.openstreetmap.josm.plugins.plbuildings.utils.JsonUtil.provider;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.enums.Notification;
import org.openstreetmap.josm.testutils.JOSMTestRules;


public class NotificationConfigTest {
    @RegisterExtension
    static JOSMTestRules rule = new JOSMTestRules();

    static final List<Notification> notifiable = List.of(
        Notification.NO_DATA, Notification.NO_UPDATE, Notification.CONNECTION_ERROR, Notification.IMPORT_ERROR
    );


    @Test
    void testLoadNotificationStates() {
        // Arrange
        BuildingsSettings.NOTIFICATION_STATES.put(
            "[{\"name\":\"connection_error\", \"isEnabled\": true},"
                + "{\"name\": \"no_update\", \"isEnabled\": true},"
                + "{\"name\": \"no_data\", \"isEnabled\": false},"
                + "{\"name\": \"import_error\", \"isEnabled\": false}]"
        );

        // Act
        NotificationConfig instance = new NotificationConfig();

        // Assert
        Assertions.assertTrue(instance.isNotificationEnabled(Notification.CONNECTION_ERROR));
        Assertions.assertTrue(instance.isNotificationEnabled(Notification.NO_UPDATE));
        Assertions.assertFalse(instance.isNotificationEnabled(Notification.NO_DATA));
        Assertions.assertFalse(instance.isNotificationEnabled(Notification.IMPORT_ERROR));
    }

    @Test
    void testSavesNotificationStates() {
        // Arrange
        NotificationConfig instance = new NotificationConfig();
        BuildingsSettings.NOTIFICATION_STATES.put("[]");

        // Act
        Arrays.stream(ImportStatus.values())
            .map(Notification::fromImportStatus)
            .filter(Objects::nonNull)
            .forEach(notification -> instance.setNotificationEnabled(notification, false));

        // Assert
        String rawJson = BuildingsSettings.NOTIFICATION_STATES.get();
        JsonReader jsonReader = provider.createReader(new StringReader(rawJson));
        JsonArray jsonStates = jsonReader.readArray();
        Assertions.assertEquals(jsonStates.size(), notifiable.size());
    }

    @Test
    void testInitDefaultStates() {
        // Arrange
        BuildingsSettings.NOTIFICATION_STATES.put("[]");

        // Act
        NotificationConfig instance = new NotificationConfig();

        // Assert
        Assertions.assertTrue(Arrays.stream(Notification.values()).allMatch(instance::isNotificationEnabled));
    }
}
