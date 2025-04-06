package org.openstreetmap.josm.plugins.plbuildings.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openstreetmap.josm.actions.ExpertToggleAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportStatus;

public class UpdateBuildingTagsCommandTest {

    @BeforeEach
    void setUp() {
        ExpertToggleAction.getInstance().setExpert(true);
    }

    @Test
    void testUnchangedTagsReturnsFalseOnExecutedCommand() {
        DataSet ds = new DataSet();

        Way selectedBuilding = new Way();
        selectedBuilding.put("building", "yes");
        ds.addPrimitiveRecursive(selectedBuilding);

        DataSet newDs = new DataSet();
        Way newBuilding = new Way();
        newBuilding.put("building", "yes");
        newDs.addPrimitiveRecursive(newBuilding);

        UpdateBuildingTagsCommand c = new UpdateBuildingTagsCommand(ds, () -> selectedBuilding, newBuilding);

        Assertions.assertFalse(c.executeCommand());
        Assertions.assertEquals("No tags to change", c.getErrorReason());
        Assertions.assertEquals(ImportStatus.NO_UPDATE, c.getErrorStatus());
    }


    @ParameterizedTest
    @CsvSource({
        "geoportal.gov.pl", "www.geoportal.gov.pl", "https://geoportal.gov.pl", "https://www.geoportal.gov.pl/", "Bing",
        "Yahoo"
    })
    void testAutoRemoveUnwantedSourceValue(String unwantedValue) {
        BuildingsSettings.AUTOREMOVE_UNWANTED_SOURCE.put(true);

        DataSet ds = new DataSet();

        Way selectedBuilding = new Way();
        selectedBuilding.put("building", "yes");
        selectedBuilding.put("source", unwantedValue.toUpperCase());
        ds.addPrimitiveRecursive(selectedBuilding);

        DataSet newDs = new DataSet();
        Way newBuilding = new Way();
        newBuilding.put("building", "yes");
        newBuilding.put("building:levels", "2");
        newDs.addPrimitiveRecursive(newBuilding);

        Command c = new UpdateBuildingTagsCommand(ds, () -> selectedBuilding, newBuilding);
        c.executeCommand();

        Assertions.assertFalse(selectedBuilding.hasTag("source"));
    }

    @Test
    void testDoNotAutoRemoveSourceWithoutUnwantedValue() {
        BuildingsSettings.AUTOREMOVE_UNWANTED_SOURCE.put(true);

        DataSet ds = new DataSet();

        Way selectedBuilding = new Way();
        selectedBuilding.put("building", "yes");
        selectedBuilding.put("source", "not_unwanted_value");
        ds.addPrimitiveRecursive(selectedBuilding);

        DataSet newDs = new DataSet();
        Way newBuilding = new Way();
        newBuilding.put("building", "yes");
        newBuilding.put("building:levels", "2");
        newDs.addPrimitiveRecursive(newBuilding);

        Command c = new UpdateBuildingTagsCommand(ds, () -> selectedBuilding, newBuilding);
        c.executeCommand();

        Assertions.assertTrue(selectedBuilding.hasTag("source"));
    }


    @Test
    void testDoNotAutoRemoveUnwantedSourceValueWithDisabledSetting() {
        BuildingsSettings.AUTOREMOVE_UNWANTED_SOURCE.put(false);

        DataSet ds = new DataSet();

        Way selectedBuilding = new Way();
        selectedBuilding.put("building", "yes");
        selectedBuilding.put(
            "source", BuildingsSettings.UNWANTED_SOURCE_VALUES.get().stream().findFirst().orElseThrow()
        );
        ds.addPrimitiveRecursive(selectedBuilding);

        DataSet newDs = new DataSet();
        Way newBuilding = new Way();
        newBuilding.put("building", "yes");
        newBuilding.put("building:levels", "2");
        newDs.addPrimitiveRecursive(newBuilding);

        Command c = new UpdateBuildingTagsCommand(ds, () -> selectedBuilding, newBuilding);
        c.executeCommand();

        Assertions.assertTrue(selectedBuilding.hasTag("source"));
    }

    @Test
    void testConstructionValueIsUsedWhenNewBuildingIsNoLongerInConstructionAndNewValueIsLessDetailed() {
        DataSet ds = new DataSet();

        Way selectedBuilding = new Way();
        selectedBuilding.put("building", "construction");
        selectedBuilding.put("construction", "house");
        ds.addPrimitiveRecursive(selectedBuilding);

        DataSet newDs = new DataSet();
        Way newBuilding = new Way();
        newBuilding.put("building", "yes");
        newDs.addPrimitiveRecursive(newBuilding);

        Command c = new UpdateBuildingTagsCommand(ds, () -> selectedBuilding, newBuilding);
        c.executeCommand();

        Assertions.assertTrue(selectedBuilding.hasTag("building", "house"));
        Assertions.assertFalse(selectedBuilding.hasTag("construction"));

        c.undoCommand();
        Assertions.assertTrue(selectedBuilding.hasTag("building", "construction"));
        Assertions.assertTrue(selectedBuilding.hasTag("construction", "house"));
    }

    @Test
    void testConstructionValueIsSkippedWhenNewBuildingIsNoLongerInConstructionAndNewValueIsMoreDetailed() {
        DataSet ds = new DataSet();

        Way selectedBuilding = new Way();
        selectedBuilding.put("building", "construction");
        selectedBuilding.put("construction", "house");
        ds.addPrimitiveRecursive(selectedBuilding);

        DataSet newDs = new DataSet();
        Way newBuilding = new Way();
        newBuilding.put("building", "detached");
        newDs.addPrimitiveRecursive(newBuilding);

        Command c = new UpdateBuildingTagsCommand(ds, () -> selectedBuilding, newBuilding);
        c.executeCommand();

        Assertions.assertTrue(selectedBuilding.hasTag("building", "detached"));
        Assertions.assertFalse(selectedBuilding.hasTag("construction"));

        c.undoCommand();
        Assertions.assertTrue(selectedBuilding.hasTag("building", "construction"));
        Assertions.assertTrue(selectedBuilding.hasTag("construction", "house"));
    }

    @Test
    void testConstructionValueIsSkippedWhenNewBuildingIsNoLongerInConstructionAndNewValueIsEqual() {
        DataSet ds = new DataSet();

        Way selectedBuilding = new Way();
        selectedBuilding.put("building", "construction");
        selectedBuilding.put("construction", "house");
        ds.addPrimitiveRecursive(selectedBuilding);

        DataSet newDs = new DataSet();
        Way newBuilding = new Way();
        newBuilding.put("building", "house");
        newDs.addPrimitiveRecursive(newBuilding);

        Command c = new UpdateBuildingTagsCommand(ds, () -> selectedBuilding, newBuilding);
        c.executeCommand();

        Assertions.assertTrue(selectedBuilding.hasTag("building", "house"));
        Assertions.assertFalse(selectedBuilding.hasTag("construction"));

        c.undoCommand();
        Assertions.assertTrue(selectedBuilding.hasTag("building", "construction"));
        Assertions.assertTrue(selectedBuilding.hasTag("construction", "house"));
    }

    @Test
    void testConstructionValueNotChangedIfNewBuildingIsInConstructionToo() {
        DataSet ds = new DataSet();

        Way selectedBuilding = new Way();
        selectedBuilding.put("building", "construction");
        selectedBuilding.put("construction", "house");
        ds.addPrimitiveRecursive(selectedBuilding);

        DataSet newDs = new DataSet();
        Way newBuilding = new Way();
        newBuilding.put("building", "construction");
        newDs.addPrimitiveRecursive(newBuilding);

        Command c = new UpdateBuildingTagsCommand(ds, () -> selectedBuilding, newBuilding);
        c.executeCommand();

        Assertions.assertTrue(selectedBuilding.hasTag("building", "construction"));
        Assertions.assertTrue(selectedBuilding.hasTag("construction", "house"));

        c.undoCommand();
        Assertions.assertTrue(selectedBuilding.hasTag("building", "construction"));
        Assertions.assertTrue(selectedBuilding.hasTag("construction", "house"));
    }
}
