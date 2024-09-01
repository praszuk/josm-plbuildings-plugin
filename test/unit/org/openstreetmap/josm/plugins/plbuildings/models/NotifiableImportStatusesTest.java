package org.openstreetmap.josm.plugins.plbuildings.models;


import static org.openstreetmap.josm.plugins.plbuildings.utils.JsonUtil.provider;

import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportStatus;
import org.openstreetmap.josm.testutils.JOSMTestRules;


public class NotifiableImportStatusesTest {
    @RegisterExtension
    static JOSMTestRules rule = new JOSMTestRules();

    static final List<ImportStatus> notifiable = List.of(
        ImportStatus.NO_DATA, ImportStatus.NO_UPDATE, ImportStatus.CONNECTION_ERROR, ImportStatus.IMPORT_ERROR
    );


    @Test
    void testLoadStatuses() {
        // Arrange
        BuildingsSettings.NOTIFIABLE_IMPORT_STATUSES.put(
            "{\"CONNECTION_ERROR\":true,\"NO_UPDATE\":true,\"NO_DATA\":false,\"IMPORT_ERROR\":false}"
        );

        // Act
        NotifiableImportStatuses instance = new NotifiableImportStatuses();

        // Assert
        Assertions.assertTrue(instance.isNotifiable(ImportStatus.CONNECTION_ERROR));
        Assertions.assertTrue(instance.isNotifiable(ImportStatus.NO_UPDATE));
        Assertions.assertFalse(instance.isNotifiable(ImportStatus.NO_DATA));
        Assertions.assertFalse(instance.isNotifiable(ImportStatus.IMPORT_ERROR));
    }

    @Test
    void testSetNotifiableIgnoresNotNotifiable() {
        // Arrange
        NotifiableImportStatuses instance = new NotifiableImportStatuses();
        BuildingsSettings.NOTIFIABLE_IMPORT_STATUSES.put("{}");

        // Act
        Arrays.stream(ImportStatus.values())
            .filter(importStatus -> !notifiable.contains(importStatus))
            .forEach(importStatus -> instance.setNotifiable(importStatus, true));

        // Assert
        Assertions.assertEquals(BuildingsSettings.NOTIFIABLE_IMPORT_STATUSES.get(), "{}");
    }

    @Test
    void testSetNotifiableSavesNotifiable() {
        // Arrange
        NotifiableImportStatuses instance = new NotifiableImportStatuses();
        BuildingsSettings.NOTIFIABLE_IMPORT_STATUSES.put("{}");

        // Act
        Arrays.stream(ImportStatus.values())
            .filter(notifiable::contains)
            .forEach(importStatus -> instance.setNotifiable(importStatus, false));

        // Assert
        String rawJson = BuildingsSettings.NOTIFIABLE_IMPORT_STATUSES.get();
        JsonReader jsonReader = provider.createReader(new StringReader(rawJson));
        JsonObject jsonStatuses = jsonReader.readObject();
        Assertions.assertEquals(jsonStatuses.entrySet().size(), notifiable.size());
    }

    @ParameterizedTest
    @EnumSource(ImportStatus.class)
    void testIsNotifiableButNotSaved(ImportStatus importStatus) {
        // Arrange
        BuildingsSettings.NOTIFIABLE_IMPORT_STATUSES.put("{}");

        // Act
        NotifiableImportStatuses instance = new NotifiableImportStatuses();

        // Assert
        boolean expected = notifiable.contains(importStatus);
        Assertions.assertEquals(expected, instance.isNotifiable(importStatus));
    }
}
