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

public class UpdateBuildingTagsCommandTest {

    @BeforeEach
    void setUp() {
        ExpertToggleAction.getInstance().setExpert(true);
    }

    @ParameterizedTest
    @CsvSource({"geoportal.gov.pl", "www.geoportal.gov.pl", "https://geoportal.gov.pl", "https://www.geoportal.gov.pl/"})
    void testAutoRemoveSourceGeoportalValue(String geoportalValue) {
        BuildingsSettings.AUTOREMOVE_SOURCE_GEOPORTAL_GOV_PL.put(true);

        DataSet ds = new DataSet();

        Way selectedBuilding = new Way();
        selectedBuilding.put("building", "yes");
        selectedBuilding.put("source", geoportalValue);
        ds.addPrimitiveRecursive(selectedBuilding);

        DataSet newDs = new DataSet();
        Way newBuilding = new Way();
        newBuilding.put("building", "yes");
        newBuilding.put("building:levels", "2");
        newDs.addPrimitiveRecursive(newBuilding);

        Command c = new UpdateBuildingTagsCommand(ds,  () -> selectedBuilding, newBuilding);
        c.executeCommand();

        Assertions.assertFalse(selectedBuilding.hasTag("source"));
    }

    @Test
    void testDoNotAutoRemoveSourceWithoutGeoportalValue() {
        BuildingsSettings.AUTOREMOVE_SOURCE_GEOPORTAL_GOV_PL.put(true);

        DataSet ds = new DataSet();

        Way selectedBuilding = new Way();
        selectedBuilding.put("building", "yes");
        selectedBuilding.put("source", "notgeoportalvalue");
        ds.addPrimitiveRecursive(selectedBuilding);

        DataSet newDs = new DataSet();
        Way newBuilding = new Way();
        newBuilding.put("building", "yes");
        newBuilding.put("building:levels", "2");
        newDs.addPrimitiveRecursive(newBuilding);

        Command c = new UpdateBuildingTagsCommand(ds,  () -> selectedBuilding, newBuilding);
        c.executeCommand();

        Assertions.assertTrue(selectedBuilding.hasTag("source"));
    }


    @Test
    void testDoNotAutoRemoveSourceGeoportalValueWithDisabledSetting() {
        BuildingsSettings.AUTOREMOVE_SOURCE_GEOPORTAL_GOV_PL.put(false);

        DataSet ds = new DataSet();

        Way selectedBuilding = new Way();
        selectedBuilding.put("building", "yes");
        selectedBuilding.put("source", "geoportal.gov.pl");
        ds.addPrimitiveRecursive(selectedBuilding);

        DataSet newDs = new DataSet();
        Way newBuilding = new Way();
        newBuilding.put("building", "yes");
        newBuilding.put("building:levels", "2");
        newDs.addPrimitiveRecursive(newBuilding);

        Command c = new UpdateBuildingTagsCommand(ds,  () -> selectedBuilding, newBuilding);
        c.executeCommand();

        Assertions.assertTrue(selectedBuilding.hasTag("source"));
    }
}
