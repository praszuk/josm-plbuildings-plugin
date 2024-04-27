package org.openstreetmap.josm.plugins.plbuildings.controllers;

import org.openstreetmap.josm.plugins.plbuildings.gui.BuildingsToggleDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;

import javax.swing.*;
import java.util.List;

public class ToggleDialogController {
    private final DataSourceConfig dataSourceConfigModel;
    private final BuildingsToggleDialog toggleDialogView;


    public ToggleDialogController(DataSourceConfig dataSourceConfig, BuildingsToggleDialog toggleDialog) { // TODO rewrite using MVC
        this.dataSourceConfigModel = dataSourceConfig;
        this.toggleDialogView = toggleDialog;


        dataSourceConfigModel.addPropertyChangeListener(DataSourceConfig.PROFILES, evt -> updateDataSourceProfileComboBox(dataSourceConfigModel.getProfiles()));
        
        toggleDialog.getDataSourceProfilesComboBox().addActionListener(
            actionEvent -> dataSourceConfig.setCurrentProfile(toggleDialogView.getSelectedDataSourceProfile())
        );
        
        updateDataSourceProfileComboBox(dataSourceConfig.getProfiles()); // TODO move to init, or sth like that
    }

    protected void updateDataSourceProfileComboBox(List<DataSourceProfile> profiles) {
        profiles.add(0, null); // add empty element to handle no profile TODO fix this logic

        this.toggleDialogView.getDataSourceProfilesComboBox().setModel(new DefaultComboBoxModel<>(profiles.toArray(DataSourceProfile[]::new)));
        this.toggleDialogView.getDataSourceProfilesComboBox().setSelectedItem(dataSourceConfigModel.getCurrentProfile());
    }

}
