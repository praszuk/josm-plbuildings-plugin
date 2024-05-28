package org.openstreetmap.josm.plugins.plbuildings.controllers;

import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsUncommonTagsPanel;
import org.openstreetmap.josm.plugins.plbuildings.models.UncommonTags;
import org.openstreetmap.josm.plugins.plbuildings.models.ui.SettingsCommonBuildingValuesListModel;

public class SettingsUncommonTagsController {
    private final UncommonTags uncommonTagsModel;
    private final SettingsUncommonTagsPanel uncommonTagsPanelView;
    private final SettingsCommonBuildingValuesListModel commonBuildingValuesListModel;

    public SettingsUncommonTagsController(UncommonTags uncommonTagsModel,
                                          SettingsUncommonTagsPanel settingsUncommonTagsPanel) {
        this.uncommonTagsModel = uncommonTagsModel;
        this.uncommonTagsPanelView = settingsUncommonTagsPanel;
        this.commonBuildingValuesListModel = new SettingsCommonBuildingValuesListModel();

        uncommonTagsPanelView.setCommonBuildingValuesListModel(commonBuildingValuesListModel);

        uncommonTagsModel.addPropertyChangeListener(
            UncommonTags.COMMON_BUILDING_VALUES, evt -> updateCommonBuildingValuesList()
        );
        initViewListeners();

        updateCommonBuildingValuesList();
    }

    private void initViewListeners() {
        uncommonTagsPanelView.addBuildingValueBtnAddActionListener(actionEvent -> addCommonBuildingValueAction());
        uncommonTagsPanelView.removeBuildingValueBtnAddActionListener(actionEvent -> removeCommonBuildingValueAction());

        uncommonTagsPanelView.commonBuildingValuesListAddListSelectionListener(
            listSelectionEvent -> uncommonTagsPanelView.removeBuildingValueBtnSetEnabled(
                uncommonTagsPanelView.getCommonBuildingValuesListSelectedIndex() != -1
            )
        );
    }

    private void addCommonBuildingValueAction() {
        String newValue = uncommonTagsPanelView.promptNewCommonBuildingValue();
        if (newValue == null || commonBuildingValuesListModel.contains(newValue)) {
            return;
        }
        uncommonTagsModel.addCommonBuildingValue(newValue);
    }

    private void removeCommonBuildingValueAction() {
        int valueIndex = uncommonTagsPanelView.getCommonBuildingValuesListSelectedIndex();
        String selectedValue = (String) commonBuildingValuesListModel.getElementAt(valueIndex);
        if (selectedValue != null) {
            uncommonTagsModel.removeCommonBuildingValue(selectedValue);
        }
    }

    private void updateCommonBuildingValuesList() {
        commonBuildingValuesListModel.clear();
        uncommonTagsModel.getCommonBuildingValues().forEach(commonBuildingValuesListModel::addElement);
    }

    public SettingsUncommonTagsPanel getSettingsUncommonTagsPanelView() {
        return uncommonTagsPanelView;
    }
}
