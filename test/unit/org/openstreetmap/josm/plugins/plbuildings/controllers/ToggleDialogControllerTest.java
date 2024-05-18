package org.openstreetmap.josm.plugins.plbuildings.controllers;

import static java.awt.Color.GRAY;
import static java.awt.Color.RED;
import static org.openstreetmap.josm.plugins.plbuildings.controllers.ToggleDialogController.COLOR_DEFAULT;
import static org.openstreetmap.josm.plugins.plbuildings.controllers.ToggleDialogController.COLOR_ORANGE;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.ACTION_REQUIRED;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.CANCELED;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.CONNECTION_ERROR;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.DONE;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.DOWNLOADING;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.IDLE;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.IMPORT_ERROR;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.NO_DATA;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.NO_UPDATE;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.stream.Stream;
import javax.swing.Timer;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;
import mockit.Verifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.gui.BuildingsToggleDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class ToggleDialogControllerTest {
    @RegisterExtension
    static JOSMTestRules rule = new JOSMTestRules();

    @Injectable
    private DataSourceConfig model;

    @Injectable
    private BuildingsToggleDialog view;

    @Tested
    private ToggleDialogController controller;


    @ParameterizedTest
    @CsvSource({"detached, detached", "\"\", --"})
    void testUpdateTagsBuildingTypeText(String buildingType, String expectedBuildingTypeText) {
        // Act
        controller.updateTags(buildingType, "", false);

        // Asserts
        new Verifications() {{
           view.setBuildingTypeText(expectedBuildingTypeText);
           times = 1;
        }};
    }

    @ParameterizedTest
    @CsvSource({"1, 1", "\"\"', --"})
    void testUpdateTagsBuildingLevels(String buildingLevels, String expectedBuildingLevelsText) {
        // Act
        controller.updateTags("", buildingLevels, false);

        // Asserts
        new Verifications() {{
            view.setBuildingLevelsText(expectedBuildingLevelsText);
        }};
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testUpdateTagsHasUncommonTag(boolean hasUncommonTag) {
        // Act
        controller.updateTags("", "", hasUncommonTag);

        // Asserts
        new Verifications() {{
            view.setHasUncommonTagText(hasUncommonTag ? tr("Yes") : tr("No"));
            view.setBuildingTypeForeground(hasUncommonTag ? COLOR_ORANGE : COLOR_DEFAULT);
            view.setHasUncommonTagForeground(hasUncommonTag ? COLOR_ORANGE : COLOR_DEFAULT);
        }};
    }

    private static Stream<Arguments> provideSetStatusData() {
        return Stream.of(
            Arguments.of(ACTION_REQUIRED, COLOR_ORANGE),
            Arguments.of(CANCELED, GRAY),
            Arguments.of(NO_DATA, GRAY),
            Arguments.of(NO_UPDATE, GRAY),
            Arguments.of(CONNECTION_ERROR, RED),
            Arguments.of(IMPORT_ERROR, RED),
            Arguments.of(IDLE, COLOR_DEFAULT),
            Arguments.of(DOWNLOADING, COLOR_DEFAULT),
            Arguments.of(DONE, COLOR_DEFAULT)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSetStatusData")
    void testSetStatusTextAndColor(ImportStatus status, Color expectedColor) {
        // Act
        controller.setStatus(status, false);

        // Asserts
        new Verifications() {{
            view.setStatusText(status.toString());
            view.setStatusForeground(expectedColor);
        }};
    }

    @Test
    void testSetStatusWithAutoChangeToDefault() {
        // Arrange
        new MockUp<GuiHelper>() {
            @Mock
            Timer scheduleTimer(int initialDelay, ActionListener actionListener, boolean repeats) {
                Timer timer = new Timer(-1, actionListener);
                timer.setRepeats(repeats);
                timer.start();
                return timer;
            }
        };
        // Act
        controller.setStatus(DONE, true);

        // Asserts
        new Verifications() {{
            controller.setStatus(IDLE, false);
        }};
    }
}
