package org.openstreetmap.josm.plugins.plbuildings.controllers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsAutoremoveSourceTagsPanel;
import org.openstreetmap.josm.plugins.plbuildings.models.AutoremoveSourceTags;
import org.openstreetmap.josm.plugins.plbuildings.models.ui.SettingsSourceValuesListModel;


public class SettingsAutoremoveSourceTagsController implements SettingsTabController {
    private final AutoremoveSourceTags sourceTagsModel;
    private final SettingsAutoremoveSourceTagsPanel sourceTagsPanelView;
    private final SettingsSourceValuesListModel sourceValuesListModel;

    public SettingsAutoremoveSourceTagsController(AutoremoveSourceTags sourceTagsModel,
                                                  SettingsAutoremoveSourceTagsPanel settingsAutoremoveSourceTagsPanel) {
        this.sourceTagsModel = sourceTagsModel;
        this.sourceTagsPanelView = settingsAutoremoveSourceTagsPanel;
        this.sourceValuesListModel = new SettingsSourceValuesListModel();

        sourceTagsPanelView.setSourceValuesListModel(sourceValuesListModel);

        sourceTagsModel.addPropertyChangeListener(
            AutoremoveSourceTags.SOURCE_VALUES, evt -> updateCommonBuildingValuesList()
        );
        initViewListeners();

        updateCommonBuildingValuesList();
    }

    private void initViewListeners() {
        sourceTagsPanelView.addSourceValueBtnAddActionListener(actionEvent -> addCommonBuildingValueAction());
        sourceTagsPanelView.removeSourceValueBtnAddActionListener(actionEvent -> removeCommonBuildingValueAction());

        sourceTagsPanelView.sourceValuesListAddListSelectionListener(
            listSelectionEvent -> sourceTagsPanelView.removeSourceValueBtnSetEnabled(
                sourceTagsPanelView.getSourceValuesListSelectedIndex() != -1
            )
        );
    }

    private void addCommonBuildingValueAction() {
        String newValue = sourceTagsPanelView.promptNewSourceValue();
        if (newValue == null || sourceValuesListModel.contains(newValue)) {
            return;
        }
        sourceTagsModel.addSourceValue(newValue);
    }

    private void removeCommonBuildingValueAction() {
        int valueIndex = sourceTagsPanelView.getSourceValuesListSelectedIndex();
        String selectedValue = (String) sourceValuesListModel.getElementAt(valueIndex);
        if (selectedValue != null) {
            sourceTagsModel.removeSourceValue(selectedValue);
        }
    }

    private void updateCommonBuildingValuesList() {
        sourceValuesListModel.clear();
        sourceTagsModel.getSourceValues().forEach(sourceValuesListModel::addElement);
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
