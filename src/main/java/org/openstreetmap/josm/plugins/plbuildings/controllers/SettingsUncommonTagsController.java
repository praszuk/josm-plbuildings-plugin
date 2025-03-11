package org.openstreetmap.josm.plugins.plbuildings.controllers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsUncommonTagsPanel;
import org.openstreetmap.josm.plugins.plbuildings.models.TagValues;
import org.openstreetmap.josm.plugins.plbuildings.models.ui.SettingsTagValuesListModel;

public class SettingsUncommonTagsController implements SettingsTabController {
    private final TagValues uncommonTagsModel;
    private final SettingsUncommonTagsPanel uncommonTagsPanelView;
    private final SettingsTagValuesListModel commonBuildingValuesListModel;

    public SettingsUncommonTagsController(TagValues tagValuesModel,
                                          SettingsUncommonTagsPanel settingsUncommonTagsPanel) {
        this.uncommonTagsModel = tagValuesModel;
        this.uncommonTagsPanelView = settingsUncommonTagsPanel;
        this.commonBuildingValuesListModel = new SettingsTagValuesListModel();

        uncommonTagsPanelView.setValuesListModel(commonBuildingValuesListModel);

        uncommonTagsModel.addPropertyChangeListener(
            TagValues.VALUES_CHANGED, evt -> updateCommonBuildingValuesList()
        );
        initViewListeners();

        updateCommonBuildingValuesList();
    }

    private void initViewListeners() {
        uncommonTagsPanelView.addValueBtnAddActionListener(actionEvent -> addCommonBuildingValueAction());
        uncommonTagsPanelView.removeValueBtnAddActionListener(actionEvent -> removeCommonBuildingValueAction());

        uncommonTagsPanelView.valuesListAddListSelectionListener(
            listSelectionEvent -> uncommonTagsPanelView.removeValueBtnSetEnabled(
                uncommonTagsPanelView.getValuesListSelectedIndex() != -1
            )
        );
    }

    private void addCommonBuildingValueAction() {
        String newValue = uncommonTagsPanelView.promptNewValue();
        if (newValue == null || commonBuildingValuesListModel.contains(newValue)) {
            return;
        }
        uncommonTagsModel.addValue(newValue);
    }

    private void removeCommonBuildingValueAction() {
        int valueIndex = uncommonTagsPanelView.getValuesListSelectedIndex();
        String selectedValue = (String) commonBuildingValuesListModel.getElementAt(valueIndex);
        if (selectedValue != null) {
            uncommonTagsModel.removeValue(selectedValue);
        }
    }

    private void updateCommonBuildingValuesList() {
        commonBuildingValuesListModel.clear();
        uncommonTagsModel.getValues().forEach(commonBuildingValuesListModel::addElement);
    }

    @Override
    public String getTabTitle() {
        return tr("Uncommon tags");
    }

    @Override
    public Component getTabView() {
        return uncommonTagsPanelView;
    }
}
