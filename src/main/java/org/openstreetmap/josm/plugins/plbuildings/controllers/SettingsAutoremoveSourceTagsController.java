package org.openstreetmap.josm.plugins.plbuildings.controllers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsAutoremoveSourceTagsPanel;
import org.openstreetmap.josm.plugins.plbuildings.models.TagValues;
import org.openstreetmap.josm.plugins.plbuildings.models.ui.SettingsTagValuesListModel;


public class SettingsAutoremoveSourceTagsController implements SettingsTabController {
    private final TagValues sourceTagsModel;
    private final SettingsAutoremoveSourceTagsPanel sourceTagsPanelView;
    private final SettingsTagValuesListModel sourceValuesListModel;

    public SettingsAutoremoveSourceTagsController(TagValues sourceValuesModel,
                                                  SettingsAutoremoveSourceTagsPanel settingsAutoremoveSourceTagsPanel) {
        this.sourceTagsModel = sourceValuesModel;
        this.sourceTagsPanelView = settingsAutoremoveSourceTagsPanel;
        this.sourceValuesListModel = new SettingsTagValuesListModel();

        sourceTagsPanelView.setValuesListModel(sourceValuesListModel);

        sourceValuesModel.addPropertyChangeListener(
            TagValues.VALUES_CHANGED, evt -> updateCommonBuildingValuesList()
        );
        initViewListeners();

        updateCommonBuildingValuesList();
    }

    private void initViewListeners() {
        sourceTagsPanelView.addValueBtnAddActionListener(actionEvent -> addCommonBuildingValueAction());
        sourceTagsPanelView.removeValueBtnAddActionListener(actionEvent -> removeCommonBuildingValueAction());

        sourceTagsPanelView.valuesListAddListSelectionListener(
            listSelectionEvent -> sourceTagsPanelView.removeValueBtnSetEnabled(
                sourceTagsPanelView.getValuesListSelectedIndex() != -1
            )
        );
    }

    private void addCommonBuildingValueAction() {
        String newValue = sourceTagsPanelView.promptNewValue();
        if (newValue == null || sourceValuesListModel.contains(newValue)) {
            return;
        }
        sourceTagsModel.addValue(newValue);
    }

    private void removeCommonBuildingValueAction() {
        int valueIndex = sourceTagsPanelView.getValuesListSelectedIndex();
        String selectedValue = (String) sourceValuesListModel.getElementAt(valueIndex);
        if (selectedValue != null) {
            sourceTagsModel.removeValue(selectedValue);
        }
    }

    private void updateCommonBuildingValuesList() {
        sourceValuesListModel.clear();
        sourceTagsModel.getValues().forEach(sourceValuesListModel::addElement);
    }

    @Override
    public String getTabTitle() {
        return tr("Autoremove source");
    }

    @Override
    public Component getTabView() {
        return sourceTagsPanelView;
    }
}
