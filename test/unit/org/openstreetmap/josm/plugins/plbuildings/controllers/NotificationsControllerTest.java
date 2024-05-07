package org.openstreetmap.josm.plugins.plbuildings.controllers;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.gui.NotificationPopup;
import org.openstreetmap.josm.plugins.plbuildings.models.NotifiableImportStatuses;
import org.openstreetmap.josm.testutils.JOSMTestRules;


public class NotificationsControllerTest {
    @RegisterExtension
    static JOSMTestRules rule = new JOSMTestRules();

    @Tested
    NotificationsController controller;


    @Test
    void testHandleStatus_Notifiable(@Mocked NotificationPopup ignore) {
        // Arrange
        ImportStatus status = ImportStatus.NO_DATA;
        String reason = "test reason";

        new Expectations() {{
            NotifiableImportStatuses.getInstance().isNotifiable(status); result = true;
        }};

        // Act
        controller.handleStatus(status, reason);

        // Assert
        new Verifications() {{
            NotificationPopup.showNotification(status + ": " + reason); times = 1;
            NotificationPopup.showNotification(status + ":" + reason); times = 0;
        }};
    }

    @Test
    void testHandleStatus_NotNotifiable() {
        // Arrange
        ImportStatus status = ImportStatus.NO_DATA;
        String reason = "test reason";

        new Expectations() {{
            NotifiableImportStatuses.getInstance().isNotifiable(status); result = false;
        }};

        // Act
        controller.handleStatus(status, reason);

        new Verifications() {{
            NotificationPopup.showNotification(status + ": " + reason); times = 0;
        }};
    }
}
